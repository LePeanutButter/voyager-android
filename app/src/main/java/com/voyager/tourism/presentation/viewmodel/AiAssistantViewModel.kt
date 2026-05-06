package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.AiChatRequestDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single chat bubble shown in the AI assistant transcript.
 *
 * @param isUser When true, the bubble represents user input; otherwise the assistant reply.
 * @param text Message body to render.
 */
data class ChatBubble(val isUser: Boolean, val text: String)

/**
 * ViewModel for the conversational Voyager AI assistant screen.
 */
@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val voyagerAiRepository: VoyagerAiRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatBubble>>(emptyList())
    val messages: StateFlow<List<ChatBubble>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Sends a user utterance to the AI service and appends the assistant reply to [messages].
     */
    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val userId = preferencesManager.getCurrentUserId()
        if (userId.isNullOrBlank()) {
            _error.value = "Inicia sesión para usar el asistente"
            return
        }
        viewModelScope.launch {
            _error.value = null
            _isSending.value = true
            _messages.value = _messages.value + ChatBubble(isUser = true, text = trimmed)
            runCatching {
                val response = voyagerAiRepository.postChat(AiChatRequestDto(userId = userId, message = trimmed))
                if (response.isSuccessful) {
                    val reply = response.body()?.reply?.take(8_000) ?: "(Sin respuesta)"
                    _messages.value = _messages.value + ChatBubble(isUser = false, text = reply)
                } else {
                    val err = response.errorBody()?.string()?.take(2_000) ?: "HTTP ${response.code()}"
                    _messages.value = _messages.value + ChatBubble(isUser = false, text = "Error: $err")
                }
            }.onFailure {
                _error.value = it.message ?: "Error de red"
                _messages.value = _messages.value + ChatBubble(isUser = false, text = "Error: ${_error.value}")
            }
            _isSending.value = false
        }
    }

    /** Clears the last error state without modifying the transcript. */
    fun clearError() {
        _error.value = null
    }
}
