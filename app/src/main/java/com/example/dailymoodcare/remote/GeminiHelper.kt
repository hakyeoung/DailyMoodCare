package com.example.dailymoodcare.remote

// Google Generative AI SDK (com.google.ai.client.generativeai:generativeai)를 사용합니다.
import com.google.ai.client.generativeai.GenerativeModel

class GeminiHelper(private val apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )
    
    suspend fun generateContent(prompt: String): String? {
        val response = generativeModel.generateContent(prompt)
        return response.text
    }
}
