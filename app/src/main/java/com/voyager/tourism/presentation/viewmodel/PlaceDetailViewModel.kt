package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.LocalRecommendationCandidateBody
import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.localai.LocalRecommendationParsers
import com.voyager.tourism.data.localai.ParsedLocalRecommendationItem
import com.voyager.tourism.data.local.PreferencesManager
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
 * Loads catalog or AI-backed place detail payloads for a selected destination or place id.
 */
@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val voyagerAiRepository: VoyagerAiRepository,
    private val preferencesManager: PreferencesManager,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _payload = MutableStateFlow<String?>(null)
    val payload: StateFlow<String?> = _payload.asStateFlow()

    private val _rankedItems = MutableStateFlow<List<ParsedLocalRecommendationItem>>(emptyList())
    val rankedItems: StateFlow<List<ParsedLocalRecommendationItem>> = _rankedItems.asStateFlow()

    /**
     * Obtiene ranking local de candidatos asociados al lugar ([placeId]) vía [postLocalRecommendations].
     */
    fun loadPlace(placeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _payload.value = null
            _rankedItems.value = emptyList()
            runCatching {
                val userId = preferencesManager.getCurrentUserId()?.takeIf { it.isNotBlank() }
                    ?: "anonymous"
                val display = placeId.replace('_', ' ').replace('-', ' ').trim().ifBlank { placeId }
                val body = LocalRecommendationRequestBody(
                    userId = userId,
                    query = "Qué hacer en $display: actividades e ideas destacadas",
                    limit = 10,
                    candidates = listOf(
                        LocalRecommendationCandidateBody(
                            id = placeId,
                            name = display,
                            category = "destination",
                            price = 0.0,
                            contentText = display,
                        ),
                        LocalRecommendationCandidateBody(
                            id = "${placeId}_explore",
                            name = "Explorar cerca de $display",
                            category = "cultural",
                            price = 0.0,
                            contentText = "ideas de viaje y experiencias",
                        ),
                    ),
                )
                val response = withContext(dispatchers.io) {
                    voyagerAiRepository.postLocalRecommendations(body)
                }
                if (response.isSuccessful) {
                    val ranked = response.body() ?: AiMatchingResponseDto(matches = emptyList(), userId = userId, totalMatches = 0)
                    _payload.value = ranked.matches.joinToString("\n") { it.name }
                    _rankedItems.value = ranked.matches.map { match ->
                        ParsedLocalRecommendationItem(
                            id = match.userId,
                            name = match.name,
                            description = match.bio.orEmpty(),
                            category = "match",
                            rating = ((match.compatibilityScore / 100.0) * 5.0).toFloat(),
                            priceLabel = when {
                                match.compatibilityScore >= 75.0 -> "$$$"
                                match.compatibilityScore >= 45.0 -> "$$"
                                else -> "$"
                            },
                        )
                    }
                } else {
                    _error.value = "HTTP ${response.code()}"
                }
            }.onFailure {
                _error.value = it.message ?: "Error de red"
            }
            _isLoading.value = false
        }
    }
}
