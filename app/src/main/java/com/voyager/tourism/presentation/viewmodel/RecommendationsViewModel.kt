package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.LocalRecommendationCandidateBody
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.localai.LocalRecommendationParsers
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Fila UI para la lista de recomendaciones rankeadas por `/local/recommendations`. */
data class RecommendationListRow(
    val id: String,
    val name: String,
    val description: String,
    val rating: Float,
    val priceLabel: String,
    val category: String,
)

/**
 * Carga recomendaciones vía [VoyagerAiRepository.postLocalRecommendations] (mismo flujo que el web:
 * candidatos del cliente + query; feedback con [postLocalRecommendationFeedback]).
 */
@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val voyagerAi: VoyagerAiRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _rows = MutableStateFlow<List<RecommendationListRow>>(emptyList())
    val rows: StateFlow<List<RecommendationListRow>> = _rows.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            runCatching {
                val userId = preferencesManager.getCurrentUserId()?.takeIf { it.isNotBlank() }
                    ?: "anonymous"
                val body = LocalRecommendationRequestBody(
                    userId = userId,
                    query = "Destinos y experiencias recomendadas para el viajero",
                    limit = 8,
                    candidates = defaultCandidates(),
                )
                val response = voyagerAi.postLocalRecommendations(body)
                if (!response.isSuccessful) {
                    _error.value = response.errorBody()?.string()?.take(1_500)
                        ?: "No se pudieron cargar recomendaciones (HTTP ${response.code()})"
                    _rows.value = emptyList()
                    return@runCatching
                }
                val text = response.body()?.string().orEmpty()
                val parsed = LocalRecommendationParsers.parseItems(text)
                _rows.value = parsed.map {
                    RecommendationListRow(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        rating = it.rating,
                        priceLabel = it.priceLabel,
                        category = it.category,
                    )
                }
                if (_rows.value.isEmpty()) {
                    _error.value = "El servicio devolvió una lista vacía."
                }
            }.onFailure {
                _error.value = it.message ?: "Error de red"
                _rows.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun submitFeedback(itemId: String, rating: Int) {
        val userId = preferencesManager.getCurrentUserId()?.takeIf { it.isNotBlank() }
        if (userId.isNullOrBlank()) {
            _feedbackMessage.value = "Inicia sesión para enviar valoración"
            return
        }
        val clamped = rating.coerceIn(1, 5)
        viewModelScope.launch {
            runCatching {
                val res = voyagerAi.postLocalRecommendationFeedback(
                    userId = userId,
                    itemId = itemId,
                    rating = clamped,
                )
                if (res.isSuccessful) {
                    _feedbackMessage.value = "Valoración enviada"
                } else {
                    _feedbackMessage.value = res.errorBody()?.string()?.take(500)
                        ?: "No se pudo registrar la valoración"
                }
            }.onFailure {
                _feedbackMessage.value = it.message ?: "Error al enviar valoración"
            }
        }
    }

    private fun defaultCandidates(): List<LocalRecommendationCandidateBody> = listOf(
        LocalRecommendationCandidateBody(
            id = "ai-item-paris",
            name = "Paris, France",
            category = "cultural",
            price = 120.0,
            contentText = "Arte, gastronomía y paseos urbanos",
        ),
        LocalRecommendationCandidateBody(
            id = "ai-item-bali",
            name = "Bali, Indonesia",
            category = "beach",
            price = 80.0,
            contentText = "Playa, relax y naturaleza tropical",
        ),
        LocalRecommendationCandidateBody(
            id = "ai-item-tokyo",
            name = "Tokyo, Japan",
            category = "urban",
            price = 150.0,
            contentText = "Ciudad moderna con tradición",
        ),
    )
}
