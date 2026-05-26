package com.example.dailymoodcare

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    private lateinit var questionGroups: List<RadioGroup>
    private lateinit var predictButton: MaterialButton
    private lateinit var sampleButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var resultCard: MaterialCardView
    private lateinit var resultTitleText: TextView
    private lateinit var healingLevelText: TextView
    private lateinit var resultDescriptionText: TextView
    private lateinit var btnRecommendVideo: MaterialButton

    private var interpreter: Interpreter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupActions()
    }

    override fun onDestroy() {
        interpreter?.close()
        interpreter = null
        super.onDestroy()
    }

    private fun bindViews() {
        questionGroups = listOf(
            findViewById(R.id.groupDifficultyFallingAsleep),
            findViewById(R.id.groupSleepHours),
            findViewById(R.id.groupWakeUpDuringNight),
            findViewById(R.id.groupSleepQuality),
            findViewById(R.id.groupConcentrationDifficulty),
            findViewById(R.id.groupDeviceBeforeSleep),
            findViewById(R.id.groupCaffeineIntake),
            findViewById(R.id.groupPhysicalActivity),
            findViewById(R.id.groupAcademicStress)
        )
        predictButton = findViewById(R.id.predictButton)
        sampleButton = findViewById(R.id.sampleButton)
        progressBar = findViewById(R.id.progressBar)
        resultCard = findViewById(R.id.resultCard)
        resultTitleText = findViewById(R.id.resultTitleText)
        healingLevelText = findViewById(R.id.healingLevelText)
        resultDescriptionText = findViewById(R.id.resultDescriptionText)
        btnRecommendVideo = findViewById(R.id.btn_recommend_video)
    }

    private fun setupActions() {
        sampleButton.setOnClickListener {
            selectSampleAnswers()
            resultCard.visibility = View.GONE
        }

        predictButton.setOnClickListener {
            val inputValues = collectInputValues()
            if (inputValues == null) {
                Snackbar.make(
                    findViewById(R.id.main),
                    "모든 질문에 답하면 피로도 예측을 시작할 수 있어요.",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            predictFatigue(inputValues)
        }
    }

    private fun collectInputValues(): FloatArray? {
        val values = FloatArray(questionGroups.size)
        questionGroups.forEachIndexed { index, group ->
            val checkedId = group.checkedRadioButtonId
            if (checkedId == -1) return null
            val selected = findViewById<RadioButton>(checkedId)
            values[index] = selected.tag.toString().toFloatOrNull() ?: return null
        }
        return values
    }

    private fun selectSampleAnswers() {
        val sampleOptionIndexes = listOf(3, 2, 2, 2, 3, 3, 2, 1, 2)
        questionGroups.forEachIndexed { index, group ->
            val optionIndex = sampleOptionIndexes[index].coerceIn(0, group.childCount - 1)
            group.check(group.getChildAt(optionIndex).id)
        }
    }

    private fun predictFatigue(inputValues: FloatArray) {
        setLoading(true)
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.Default) {
                    runModel(inputValues)
                }
            }

            setLoading(false)
            result
                .onSuccess { showResult(it) }
                .onFailure {
                    resultCard.visibility = View.GONE
                    Snackbar.make(
                        findViewById(R.id.main),
                        "모델 예측을 완료하지 못했습니다. 모델 파일과 입력값을 확인해 주세요.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun runModel(inputValues: FloatArray): PredictionResult {
        val model = interpreter ?: Interpreter(loadModelFile()).also { interpreter = it }
        val inputBuffer = buildInputBuffer(model, inputValues)
        val outputTensor = model.getOutputTensor(0)
        val outputCount = outputTensor.shape().fold(1) { total, size -> total * size }
        val outputBuffer = ByteBuffer
            .allocateDirect(outputCount * outputTensor.dataType().byteSize())
            .order(ByteOrder.nativeOrder())

        model.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        val rawOutputs = readOutputValues(outputBuffer, outputTensor.dataType(), outputCount)
        val classIndex = resolveClassIndex(rawOutputs).coerceIn(0, fatigueLabels.lastIndex)
        return PredictionResult(
            label = fatigueLabels[classIndex],
            healingLevel = healingLevels[classIndex],
            description = resultDescriptions[classIndex]
        )
    }

    private fun buildInputBuffer(model: Interpreter, inputValues: FloatArray): ByteBuffer {
        val inputTensor = model.getInputTensor(0)
        val expectedFeatureCount = inputTensor.shape().last()
        require(expectedFeatureCount == inputValues.size) {
            "Model expects $expectedFeatureCount values, but ${inputValues.size} were provided."
        }

        val inputBuffer = ByteBuffer
            .allocateDirect(inputValues.size * inputTensor.dataType().byteSize())
            .order(ByteOrder.nativeOrder())
        inputValues.forEach { value ->
            when (inputTensor.dataType()) {
                DataType.FLOAT32 -> inputBuffer.putFloat(value)
                DataType.INT32 -> inputBuffer.putInt(value.toInt())
                else -> error("Unsupported input tensor type: ${inputTensor.dataType()}")
            }
        }
        inputBuffer.rewind()
        return inputBuffer
    }

    private fun readOutputValues(
        outputBuffer: ByteBuffer,
        dataType: DataType,
        outputCount: Int
    ): FloatArray {
        return FloatArray(outputCount) {
            when (dataType) {
                DataType.FLOAT32 -> outputBuffer.float
                DataType.INT32 -> outputBuffer.int.toFloat()
                DataType.UINT8 -> (outputBuffer.get().toInt() and 0xFF).toFloat()
                else -> error("Unsupported output tensor type: $dataType")
            }
        }
    }

    private fun resolveClassIndex(outputs: FloatArray): Int {
        if (outputs.size == 1) return outputs.first().toInt()
        return outputs.indices.maxBy { outputs[it] }
    }

    private fun showResult(result: PredictionResult) {
        resultTitleText.text = result.label
        healingLevelText.text = result.healingLevel
        resultDescriptionText.text = result.description
        resultCard.visibility = View.VISIBLE
        
        btnRecommendVideo.setOnClickListener {
            val intent = android.content.Intent(this, VideoRecommendActivity::class.java).apply {
                putExtra("USER_CONDITION", result.label)
                putExtra("HEALING_LEVEL", result.healingLevel)
            }
            startActivity(intent)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        predictButton.isEnabled = !isLoading
        sampleButton.isEnabled = !isLoading
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd(MODEL_FILE_NAME)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            return inputStream.channel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    private fun DataType.byteSize(): Int {
        return when (this) {
            DataType.FLOAT32, DataType.INT32 -> 4
            DataType.UINT8, DataType.INT8, DataType.BOOL -> 1
            DataType.INT64 -> 8
            else -> error("Unsupported tensor type: $this")
        }
    }

    private data class PredictionResult(
        val label: String,
        val healingLevel: String,
        val description: String
    )

    private companion object {
        const val MODEL_FILE_NAME = "moodrest_fatigue_model.tflite"

        val fatigueLabels = listOf("낮은 피로도", "보통 피로도", "높은 피로도")
        val healingLevels = listOf("작은 힐링 필요", "중간 힐링 필요", "큰 힐링 필요")
        val resultDescriptions = listOf(
            "현재 피로도가 낮으므로 짧은 휴식과 컨디션 유지 루틴을 추천합니다.",
            "피로가 누적될 가능성이 있으므로 가벼운 힐링 루틴을 추천합니다.",
            "피로도가 높게 예측되므로 충분한 휴식과 강한 힐링 루틴을 추천합니다."
        )
    }
}
