package com.example.dailymoodcare

import android.content.Intent
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
import com.example.dailymoodcare.repository.YouTubeRepository
import com.example.dailymoodcare.ui.VideoAdapter
import kotlinx.coroutines.launch

class VideoRecommendActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var tvGeminiAdvice: TextView
    private lateinit var progressBarVideos: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_recommend)

        recyclerView = findViewById(R.id.rv_videos)
        tvGeminiAdvice = findViewById(R.id.tv_gemini_advice)
        progressBarVideos = findViewById(R.id.progressBarVideos)

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

        loadGeminiAdvice(userCondition)
        loadYouTubeVideos(healingLevel)
    }

    private fun loadGeminiAdvice(condition: String) {
        val geminiKey = BuildConfig.GEMINI_API_KEY
        val geminiRepository = GeminiRepository(GeminiHelper(geminiKey))

        lifecycleScope.launch {
            val advice = geminiRepository.getHealingRecommendation(condition)
            tvGeminiAdvice.text = advice
        }
    }

    private fun loadYouTubeVideos(healingLevel: String) {
        progressBarVideos.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        val youtubeKey = BuildConfig.YOUTUBE_API_KEY
        val youtubeRepository = YouTubeRepository(RetrofitClient.youtubeApiService, youtubeKey)

        lifecycleScope.launch {
            val query = "$healingLevel 휴식 명상 음악"
            val videos = youtubeRepository.searchHealingVideos(query)
            
            progressBarVideos.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            
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
}
