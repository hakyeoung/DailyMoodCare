package com.example.dailymoodcare.remote

import com.example.dailymoodcare.data.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("getUltraSrtNcst")
    suspend fun getUltraShortNowcast(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 1000,
        @Query("dataType") dataType: String = "JSON",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int = 98,
        @Query("ny") ny: Int = 76
    ): WeatherResponse
}
