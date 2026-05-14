package com.nammaskill.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AnthropicApiService {
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicRequest
    ): AnthropicResponse
}

data class AnthropicRequest(
    val model: String,
    val max_tokens: Int,
    val system: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class AnthropicResponse(
    val content: List<Content>
)

data class Content(
    val text: String,
    val type: String
)
