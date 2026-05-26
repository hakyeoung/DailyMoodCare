package com.example.dailymoodcare.repository

import com.example.dailymoodcare.data.VideoItem
import com.example.dailymoodcare.remote.YouTubeApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YouTubeRepository(
    private val apiService: YouTubeApiService,
    private val apiKey: String
) {
    suspend fun searchHealingVideos(query: String): List<VideoItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchVideos(query = query, apiKey = apiKey)
                response.items.mapNotNull { item ->
                    // videoId가 없는 경우는 제외
                    item.id.videoId?.let { videoId ->
                        VideoItem(
                            id = videoId,
                            title = item.snippet.title,
                            thumbnailUrl = item.snippet.thumbnails.high.url,
                            description = item.snippet.description
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
