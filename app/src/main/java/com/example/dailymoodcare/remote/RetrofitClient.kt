package com.example.dailymoodcare.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"

    val youtubeApiService: YouTubeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(YOUTUBE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApiService::class.java)
    }
}
