package com.nammaskill.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammaskill.BuildConfig
import com.nammaskill.data.remote.GeminiApiService
import com.nammaskill.data.remote.GeminiRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val content: String,
    val isUser: Boolean
)

@HiltViewModel
class GuidanceViewModel @Inject constructor(
    private val apiService: GeminiApiService
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I am your AI career counselor. How can I help you today?", false)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val systemInstruction = "You are a helpful career counselor for rural Indian youth. Suggest vocational trades like Welding, Electrician, or Coding based on their interests. Keep answers simple and encouraging."

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val currentMessages = _chatMessages.value.toMutableList()
        currentMessages.add(ChatMessage(userMessage, true))
        _chatMessages.value = currentMessages

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiRequest.Content(
                            parts = listOf(GeminiRequest.Part(text = userMessage))
                        )
                    ),
                    systemInstruction = GeminiRequest.SystemInstruction(
                        parts = listOf(GeminiRequest.Part(text = systemInstruction))
                    )
                )

                val response = apiService.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    request = request
                )

                val aiResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "I'm sorry, I couldn't process that right now."
                
                val updatedMessages = _chatMessages.value.toMutableList()
                updatedMessages.add(ChatMessage(aiResponse, false))
                _chatMessages.value = updatedMessages
            } catch (e: Exception) {
                val updatedMessages = _chatMessages.value.toMutableList()
                updatedMessages.add(ChatMessage("Connection error. Please check your internet and try again.", false))
                _chatMessages.value = updatedMessages
            } finally {
                _isLoading.value = false
            }
        }
    }
}
