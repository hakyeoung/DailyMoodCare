package com.example.dailymoodcare

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ModelInstrumentedTest {

    @Test
    fun printFatigueModelPredictions() {
        Interpreter(loadModelFile()).use { model ->
            val cases = mapOf(
                "healthy_xml_tags" to floatArrayOf(0f, 4f, 0f, 4f, 0f, 0f, 0f, 4f, 1f),
                "healthy_with_zero_stress" to floatArrayOf(0f, 4f, 0f, 4f, 0f, 0f, 0f, 4f, 0f),
                "healthy_reversed_good_features" to floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 1f),
                "all_zero" to floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
                "all_one" to floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f),
                "mostly_good_low_activity" to floatArrayOf(0f, 4f, 0f, 4f, 0f, 0f, 0f, 0f, 0f),
                "mostly_good_average_sleep" to floatArrayOf(0f, 2f, 0f, 4f, 0f, 0f, 0f, 4f, 0f),
                "middle" to floatArrayOf(2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 1f),
                "tired" to floatArrayOf(4f, 0f, 4f, 0f, 4f, 4f, 4f, 0f, 3f)
            )

            cases.forEach { (name, values) ->
                val outputs = runModel(model, values)
                val classIndex = if (outputs.size == 1) {
                    outputs.first().toInt()
                } else {
                    outputs.indices.maxBy { outputs[it] }
                }
                Log.i(TAG, "$name input=${values.joinToString()} output=${outputs.joinToString()} class=$classIndex")
            }
        }
    }

    private fun runModel(model: Interpreter, inputValues: FloatArray): FloatArray {
        val inputTensor = model.getInputTensor(0)
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

        val outputTensor = model.getOutputTensor(0)
        val outputCount = outputTensor.shape().fold(1) { total, size -> total * size }
        val outputBuffer = ByteBuffer
            .allocateDirect(outputCount * outputTensor.dataType().byteSize())
            .order(ByteOrder.nativeOrder())

        model.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()

        return FloatArray(outputCount) {
            when (outputTensor.dataType()) {
                DataType.FLOAT32 -> outputBuffer.float
                DataType.INT32 -> outputBuffer.int.toFloat()
                DataType.UINT8 -> (outputBuffer.get().toInt() and 0xFF).toFloat()
                else -> error("Unsupported output tensor type: ${outputTensor.dataType()}")
            }
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fileDescriptor = context.assets.openFd(MODEL_FILE_NAME)
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

    private companion object {
        const val MODEL_FILE_NAME = "moodrest_fatigue_model.tflite"
        const val TAG = "FatigueModelTest"
    }
}
