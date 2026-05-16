package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dashboard.AiDashboardParsers
import com.voyager.tourism.data.dto.AiTrendDashboardDto
import com.voyager.tourism.data.dto.LocalChatRequestBody
import com.voyager.tourism.data.dto.LocalRecommendationCandidateBody
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.localai.LocalChatHistoryParsers
import com.voyager.tourism.data.localai.LocalRecommendationParsers
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Single chat bubble shown in the AI assistant transcript.
 *
 * @param isUser When true, the bubble represents user input; otherwise the assistant reply.
 * @param text Message body to render.
 */
data class ChatBubble(val isUser: Boolean, val text: String)

/**
 * Asistente alineado con el web: hook useAIChat — POST /local/chat/message, historial por sesión
 * (PreferencesManager + GET /local/chat/history), candidatos desde tendencias y ranking opcional
 * con postLocalRecommendations si el texto contiene palabras clave como en el web.
 */
@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val voyagerAiRepository: VoyagerAiRepository,
    private val preferencesManager: PreferencesManager,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatBubble>>(emptyList())
    val messages: StateFlow<List<ChatBubble>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _loadingHistory = MutableStateFlow(false)
    val loadingHistory: StateFlow<Boolean> = _loadingHistory.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var recommendationPool: List<LocalRecommendationCandidateBody> = emptyList()

    /**
     * Carga sesión, historial local y candidatos de tendencias (como el web al montar el hook).
     */
    fun ensureInitialized() {
        if (_loadingHistory.value) return
        val userId = preferencesManager.getCurrentUserId()?.takeIf { it.isNotBlank() } ?: run {
            _messages.value = emptyList()
            return
        }
        viewModelScope.launch {
            _loadingHistory.value = true
            _error.value = null
            runCatching {
                val sessionId = preferencesManager.getOrCreateLocalChatSessionId(userId)
                withContext(dispatchers.io) { refreshRecommendationPool() }
                loadHistoryIntoMessages(sessionId)
            }.onFailure {
                _error.value = it.message ?: "Error al cargar el asistente"
                _messages.value = listOf(
                    ChatBubble(isUser = false, text = welcomeFallback()),
                )
            }
            _loadingHistory.value = false
        }
    }

    /**
     * Envía un turno a [postLocalChatMessage] y, si aplica, enriquece con ranking local (misma heurística que el web).
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
            val sessionId = preferencesManager.getOrCreateLocalChatSessionId(userId)
            
            try {
                val reply = sendMessageAndGetReply(userId, sessionId, trimmed)
                _messages.value = _messages.value + ChatBubble(isUser = false, text = reply)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error de red"
                _messages.value = _messages.value + ChatBubble(
                    isUser = false,
                    text = "Error: ${_error.value}",
                )
            }
            _isSending.value = false
        }
    }

    private suspend fun sendMessageAndGetReply(userId: String, sessionId: String, message: String): String {
        val chatRes = voyagerAiRepository.postLocalChatMessage(
            LocalChatRequestBody(userId = userId, sessionId = sessionId, message = message),
        )
        if (!chatRes.isSuccessful) {
            val err = chatRes.errorBody()?.string()?.take(2_000) ?: "HTTP ${chatRes.code()}"
            throw Exception("Error: $err")
        }
        var reply = chatRes.body()?.reply?.take(8_000)?.ifBlank { "(Sin respuesta)" } ?: "(Sin respuesta)"
        
        if (wantsLocalRanking(message) && recommendationPool.isNotEmpty()) {
            reply = enrichReplyWithRecommendations(userId, message, reply)
        }
        
        return reply
    }

    private suspend fun enrichReplyWithRecommendations(userId: String, message: String, reply: String): String {
        return try {
            val rankRes = withContext(dispatchers.io) {
                voyagerAiRepository.postLocalRecommendations(
                    LocalRecommendationRequestBody(
                        userId = userId,
                        query = message,
                        limit = 5,
                        candidates = recommendationPool,
                    ),
                )
            }
            if (rankRes.isSuccessful) {
                val names = rankRes.body()?.matches.orEmpty()
                    .map { it.name }
                    .filter { it.isNotBlank() }
                    .take(5)
                if (names.isNotEmpty()) {
                    reply + "\n\nSugerencias: ${names.joinToString(", ")}"
                } else {
                    reply
                }
            } else {
                reply
            }
        } catch (e: Exception) {
            reply
        }
    }

    /** Nueva conversación: rota sesión como `rotateLocalChatSessionId` en el web. */
    fun clearConversation() {
        val userId = preferencesManager.getCurrentUserId()?.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            preferencesManager.rotateLocalChatSessionId(userId)
            _messages.value = listOf(ChatBubble(isUser = false, text = welcomeNewSession()))
            _error.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }

    private suspend fun refreshRecommendationPool() {
        runCatching {
            val res = voyagerAiRepository.getTrendsDashboard()
            if (!res.isSuccessful) {
                recommendationPool = emptyList()
                return
            }
            val body: AiTrendDashboardDto = res.body() ?: run {
                recommendationPool = emptyList()
                return
            }
            recommendationPool = body.trendingDestinations.map { d ->
                LocalRecommendationCandidateBody(
                    id = d.id ?: d.name,
                    name = d.name,
                    category = "destination",
                    price = 0.0,
                    contentText = d.name,
                )
            }.take(25)
        }.onFailure {
            recommendationPool = emptyList()
        }
    }

    private suspend fun loadHistoryIntoMessages(sessionId: String) {
        val histRes = withContext(dispatchers.io) {
            voyagerAiRepository.getLocalChatHistory(sessionId = sessionId, limit = 50)
        }
        if (!histRes.isSuccessful) {
            _messages.value = listOf(ChatBubble(isUser = false, text = welcomeFallback()))
            return
        }
        val bodyList = histRes.body()
        val lines = mutableListOf<Pair<Boolean, String>>()
        if (bodyList != null && bodyList.isNotEmpty()) {
            // Convert typed LocalChatResponseDto list into display pairs (assistant replies assumed)
            for (item in bodyList) {
                val text = item.reply.ifBlank { "(Sin respuesta)" }
                lines.add(false to text)
            }
        } else {
            // fallback to legacy raw parsing if any (defensive)
            val raw = ""
            val parsed = LocalChatHistoryParsers.parseMessages(raw)
            lines.addAll(parsed)
        }

        if (lines.isEmpty()) {
            _messages.value = listOf(ChatBubble(isUser = false, text = welcomeEmptyHistory()))
            return
        }

        _messages.value = lines.map { (isUser, t) -> ChatBubble(isUser = isUser, text = t) }
    }

    private fun wantsLocalRanking(message: String): Boolean {
        val t = message.lowercase()
        return RANK_TRIGGER_SUBSTRINGS.any { t.contains(it) }
    }

    private fun welcomeEmptyHistory(): String =
        "Hola. Soy Voyager IA (modo local). Pregunta por destinos, itinerarios o presupuestos."

    private fun welcomeFallback(): String =
        "Hola. Soy Voyager IA. Pregunta lo que quieras sobre destinos, itinerarios o presupuestos."

    private fun welcomeNewSession(): String =
        "Conversación nueva. ¿En qué puedo ayudarte?"

    private companion object {
        /** Mismas palabras clave que [useAIChat.js] en el web. */
        val RANK_TRIGGER_SUBSTRINGS = listOf("recom", "suger", "producto", "comprar")
    }
}
