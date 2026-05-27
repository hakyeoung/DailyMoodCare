package com.example.dailymoodcare.repository

import com.example.dailymoodcare.remote.GeminiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository(private val geminiHelper: GeminiHelper) {

    suspend fun getHealingRecommendation(userCondition: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = "사용자의 현재 컨디션은 '$userCondition' 입니다. 이 사용자를 위한 힐링 루틴이나 조언을 짧게 추천해주세요."
                val result = geminiHelper.generateContent(prompt)
                result ?: "추천을 가져오지 못했습니다. 잠시 후 다시 시도해주세요."
            } catch (e: Exception) {
                e.printStackTrace()
                "오류가 발생했습니다: ${e.localizedMessage}"
            }
        }
    }

    suspend fun getBusanHealingPlaceRecommendation(
        stressLevel: String,
        healingLevel: String,
        weatherSummary: String
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    사용자의 머신 러닝 예측 스트레스/피로 상태는 '$stressLevel'이고, 필요한 회복 강도는 '$healingLevel'입니다.
                    오늘 날씨 정보는 '$weatherSummary'입니다.
                    이 조건에 맞춰 부산에서 가기 좋은 힐링 장소 3곳을 추천해주세요.
                    각 장소마다 장소명, 추천 이유, 날씨를 고려한 짧은 방문 팁을 포함하고 한국어로 간결하게 답해주세요.
                """.trimIndent()
                val result = geminiHelper.generateContent(prompt)
                result ?: "추천을 가져오지 못했습니다. 잠시 후 다시 시도해주세요."
            } catch (e: Exception) {
                e.printStackTrace()
                "오류가 발생했습니다: ${e.localizedMessage}"
            }
        }
    }
}
