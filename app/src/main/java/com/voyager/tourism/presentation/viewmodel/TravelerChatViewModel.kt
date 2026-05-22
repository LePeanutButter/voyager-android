package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.MessageDto
import com.voyager.tourism.data.dto.SendMessageRequestDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import com.voyager.tourism.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class TravelerChatMessageUi(
    val key: String,
    val id: Long? = null,
    val connectionId: Long? = null,
    val senderId: Long? = null,
    val content: String,
    val createdAt: String? = null,
    val status: String? = null,
)

data class TravelerChatUiState(
    val isLoadingHistory: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val messages: List<TravelerChatMessageUi> = emptyList(),
)

@HiltViewModel
class TravelerChatViewModel @Inject constructor(
    private val backendSupplementRepository: BackendSupplementRepository,
    private val preferencesManager: PreferencesManager,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelerChatUiState())
    val uiState: StateFlow<TravelerChatUiState> = _uiState.asStateFlow()

    fun loadConversation(connectionId: Long) = viewModelScope.launch {
        reloadConversation(connectionId)
    }

    fun refreshConversation(connectionId: Long) = viewModelScope.launch {
        if (_uiState.value.isLoadingHistory) return@launch
        reloadConversation(connectionId, showLoading = false)
    }

    suspend fun sendMessage(connectionId: Long, text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return false
        val senderId = preferencesManager.getCurrentUserId()?.toLongOrNull() ?: run {
            _uiState.update { it.copy(error = "Inicia sesión para usar el chat") }
            return false
        }

        _uiState.update { it.copy(isSending = true, error = null) }
        return try {
            val response = withContext(dispatchers.io) {
                backendSupplementRepository.sendMessage(
                    SendMessageRequestDto(
                        connectionId = connectionId,
                        senderId = senderId,
                        content = trimmed,
                    ),
                )
            }
            val sent = requireMessage(response.data, response.message).toUiMessage()
            _uiState.update { state ->
                state.copy(
                    isSending = false,
                    messages = mergeMessages(state.messages, listOf(sent)),
                )
            }
            true
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isSending = false,
                    error = e.message ?: "No se pudo enviar el mensaje",
                )
            }
            false
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun reloadConversation(connectionId: Long, showLoading: Boolean = true) {
        val userId = preferencesManager.getCurrentUserId()?.toLongOrNull()
        if (userId == null) {
            _uiState.update { it.copy(error = "Inicia sesión para ver el chat", isLoadingHistory = false) }
            return
        }

        if (showLoading) {
            _uiState.update { it.copy(isLoadingHistory = true, error = null) }
        }

        try {
            val messages = loadAllMessages(connectionId, userId)
            _uiState.update { state ->
                state.copy(
                    isLoadingHistory = false,
                    messages = messages,
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoadingHistory = false,
                    error = e.message ?: "No se pudo cargar la conversación",
                )
            }
        }
    }

    private suspend fun loadAllMessages(connectionId: Long, userId: Long): List<TravelerChatMessageUi> {
        val aggregated = ArrayList<MessageDto>()
        val pageSize = 50
        val maxPages = 100

        for (page in 0 until maxPages) {
            val response = withContext(dispatchers.io) {
                backendSupplementRepository.getConversationMessages(connectionId, userId, page, pageSize)
            }
            if (response.status !in 200..299) {
                throw IllegalStateException(response.message.ifBlank { "HTTP ${response.status}" })
            }
            val pageMessages = response.data.orEmpty()
            aggregated.addAll(pageMessages)

            if (response.last || pageMessages.isEmpty() || pageMessages.size < pageSize) {
                break
            }
            if (response.totalPages > 0 && page >= response.totalPages - 1) {
                break
            }
        }

        return aggregated.mapIndexed { index, dto ->
            dto.toUiMessage(fallbackKey = "${dto.id ?: "msg-$index"}-${dto.createdAt.orEmpty()}")
        }.let(::mergeMessages)
    }

    private fun mergeMessages(messages: List<TravelerChatMessageUi>): List<TravelerChatMessageUi> =
        messages
            .distinctBy { it.key }
            .sortedWith(
                compareBy<TravelerChatMessageUi> { it.createdAt.orEmpty() }
                    .thenBy { it.key },
            )

    private fun mergeMessages(
        current: List<TravelerChatMessageUi>,
        next: List<TravelerChatMessageUi>,
    ): List<TravelerChatMessageUi> = mergeMessages(current + next)

    private fun MessageDto.toUiMessage(fallbackKey: String? = null): TravelerChatMessageUi {
        val key = id?.toString() ?: fallbackKey ?: "${senderId ?: 0L}-${createdAt.orEmpty()}-${content.orEmpty()}"
        return TravelerChatMessageUi(
            key = key,
            id = id,
            connectionId = connectionId,
            senderId = senderId,
            content = content.orEmpty(),
            createdAt = createdAt,
            status = status,
        )
    }

    private fun requireMessage(message: MessageDto?, fallbackMessage: String): MessageDto {
        if (message != null) return message
        throw IllegalStateException(fallbackMessage.ifBlank { "No se recibió el mensaje" })
    }
}