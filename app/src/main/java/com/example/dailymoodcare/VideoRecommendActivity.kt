package com.example.dailymoodcare

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailymoodcare.data.VideoItem
import com.example.dailymoodcare.remote.GeminiHelper
import com.example.dailymoodcare.remote.RetrofitClient
import com.example.dailymoodcare.repository.GeminiRepository
import com.example.dailymoodcare.repository.WeatherRepository
import com.example.dailymoodcare.repository.YouTubeRepository
import com.example.dailymoodcare.ui.VideoAdapter
import kotlinx.coroutines.launch

class VideoRecommendActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var tvRecommendTitle: TextView
    private lateinit var tvWeatherInfo: TextView
    private lateinit var tvGeminiAdvice: TextView
    private lateinit var progressBarVideos: ProgressBar
    private lateinit var tvVideoEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_recommend)

        recyclerView = findViewById(R.id.rv_videos)
        tvRecommendTitle = findViewById(R.id.tv_recommend_title)
        tvWeatherInfo = findViewById(R.id.tv_weather_info)
        tvGeminiAdvice = findViewById(R.id.tv_gemini_advice)
        progressBarVideos = findViewById(R.id.progressBarVideos)
        tvVideoEmpty = findViewById(R.id.tv_video_empty)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter 초기화 (빈 리스트로 시작)
        videoAdapter = VideoAdapter(emptyList()) { videoItem ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("VIDEO_ID", videoItem.id)
                putExtra("VIDEO_TITLE", videoItem.title)
                putExtra("VIDEO_DESC", videoItem.description)
            }
            startActivity(intent)
        }
        recyclerView.adapter = videoAdapter

        // MainActivity에서 전달된 상태 정보 받기
        val userCondition = intent.getStringExtra("USER_CONDITION") ?: "피로도 알 수 없음"
        val healingLevel = intent.getStringExtra("HEALING_LEVEL") ?: "힐링"

        loadGeminiAdvice(userCondition, healingLevel)
        loadYouTubeVideos(healingLevel)
    }

    private fun loadGeminiAdvice(condition: String, healingLevel: String) {
        val geminiKey = BuildConfig.GEMINI_API_KEY
        val weatherKey = BuildConfig.WEATHER_API_KEY

        if (!isNetworkAvailable()) {
            tvWeatherInfo.text = "네트워크 연결을 확인해 주세요."
            tvGeminiAdvice.text = "네트워크 연결 후 부산 힐링 장소 추천을 다시 확인할 수 있습니다."
            return
        }

        if (geminiKey.isBlank() || weatherKey.isBlank()) {
            tvWeatherInfo.text = "날씨 또는 Gemini API 키가 설정되지 않았습니다."
            tvGeminiAdvice.text = "local.properties에 GEMINI_API_KEY와 WEATHER_API_KEY를 설정해 주세요."
            return
        }

        val geminiRepository = GeminiRepository(GeminiHelper(geminiKey))
        val weatherRepository = WeatherRepository(
            RetrofitClient.weatherApiService,
            weatherKey
        )

        lifecycleScope.launch {
            tvGeminiAdvice.text = "부산 현재 날씨와 스트레스 정도를 확인해 힐링 장소를 추천받는 중입니다..."
            val weatherSummary = weatherRepository.getBusanCurrentWeatherSummary()
            tvWeatherInfo.text = weatherSummary
            tvRecommendTitle.text = "추천 여행지"
            val advice = geminiRepository.getBusanHealingPlaceRecommendation(
                stressLevel = condition,
                healingLevel = healingLevel,
                weatherSummary = weatherSummary
            )
            tvGeminiAdvice.text = advice
        }
    }

    private fun loadYouTubeVideos(healingLevel: String) {
        val youtubeKey = BuildConfig.YOUTUBE_API_KEY
        if (!isNetworkAvailable()) {
            showVideoMessage("네트워크 연결을 확인해 주세요.")
            return
        }

        if (youtubeKey.isBlank()) {
            showVideoMessage("YouTube API 키가 설정되지 않았습니다. local.properties를 확인해 주세요.")
            return
        }

        progressBarVideos.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvVideoEmpty.visibility = View.GONE

        val youtubeRepository = YouTubeRepository(RetrofitClient.youtubeApiService, youtubeKey)

        lifecycleScope.launch {
            val query = "$healingLevel 휴식 명상 음악"
            val videos = youtubeRepository.searchHealingVideos(query)
            
            progressBarVideos.visibility = View.GONE
            if (videos.isEmpty()) {
                showVideoMessage("추천 영상을 찾지 못했습니다. 잠시 후 다시 시도해 주세요.")
                return@launch
            }

            recyclerView.visibility = View.VISIBLE
            tvVideoEmpty.visibility = View.GONE
            
            videoAdapter = VideoAdapter(videos) { videoItem ->
                val intent = Intent(this@VideoRecommendActivity, DetailActivity::class.java).apply {
                    putExtra("VIDEO_ID", videoItem.id)
                    putExtra("VIDEO_TITLE", videoItem.title)
                    putExtra("VIDEO_DESC", videoItem.description)
                }
                startActivity(intent)
            }
            recyclerView.adapter = videoAdapter
        }
    }

    private fun showVideoMessage(message: String) {
        progressBarVideos.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvVideoEmpty.text = message
        tvVideoEmpty.visibility = View.VISIBLE
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
