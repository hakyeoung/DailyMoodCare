package com.example.dailymoodcare.repository

import com.example.dailymoodcare.remote.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val apiKey: String
) {
    suspend fun getBusanCurrentWeatherSummary(): String {
        return withContext(Dispatchers.IO) {
            try {
                val baseDateTime = resolveBaseDateTime()
                val response = weatherApiService.getUltraShortNowcast(
                    serviceKey = apiKey,
                    baseDate = baseDateTime.date,
                    baseTime = baseDateTime.time
                )

                val header = response.response?.header
                if (header?.resultCode != "00") {
                    return@withContext "부산 날씨 정보를 가져오지 못했습니다(${header?.resultMsg ?: "응답 오류"})."
                }

                val values = response.response?.body?.items?.item
                    .orEmpty()
                    .mapNotNull { item ->
                        val category = item.category ?: return@mapNotNull null
                        val value = item.obsrValue ?: return@mapNotNull null
                        category to value
                    }
                    .toMap()

                buildWeatherSummary(values)
            } catch (e: Exception) {
                "부산 날씨 정보를 가져오지 못했습니다(${e.localizedMessage ?: "네트워크 오류"})."
            }
        }
    }

    private fun resolveBaseDateTime(): BaseDateTime {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        if (calendar.get(Calendar.MINUTE) < 45) {
            calendar.add(Calendar.HOUR_OF_DAY, -1)
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        val timeFormat = SimpleDateFormat("HH00", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }

        return BaseDateTime(
            date = dateFormat.format(calendar.time),
            time = timeFormat.format(calendar.time)
        )
    }

    private fun buildWeatherSummary(values: Map<String, String>): String {
        val temperature = values["T1H"]?.let { "${it}도" } ?: "기온 정보 없음"
        val rain = values["RN1"]?.let { "1시간 강수량 ${it}mm" } ?: "강수량 정보 없음"
        val humidity = values["REH"]?.let { "습도 ${it}%" } ?: "습도 정보 없음"
        val wind = values["WSD"]?.let { "풍속 ${it}m/s" } ?: "풍속 정보 없음"

        return "부산 현재 날씨: $temperature, $rain, $humidity, $wind"
    }

    private data class BaseDateTime(
        val date: String,
        val time: String
    )
}
