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
}
