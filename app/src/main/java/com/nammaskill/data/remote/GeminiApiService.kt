package com.nammaskill.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: SystemInstruction? = null
) {
    data class Content(
        val parts: List<Part>
    )
    data class Part(
        val text: String
    )
    data class SystemInstruction(
        val parts: List<Part>
    )
}

data class GeminiResponse(
    val candidates: List<Candidate>
) {
    data class Candidate(
        val content: Content
    )
    data class Content(
        val parts: List<Part>
    )
    data class Part(
        val text: String
    )
}
