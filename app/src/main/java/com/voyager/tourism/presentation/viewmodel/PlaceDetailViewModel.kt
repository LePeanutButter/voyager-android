package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.LocalRecommendationCandidateBody
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Loads catalog or AI-backed place detail payloads for a selected destination or place id.
 */
@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val voyagerAiRepository: VoyagerAiRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _payload = MutableStateFlow<String?>(null)
    val payload: StateFlow<String?> = _payload.asStateFlow()

    /**
     * Obtiene ranking local de candidatos asociados al lugar ([placeId]) vía [postLocalRecommendations].
     */
    fun loadPlace(placeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _payload.value = null
            runCatching {
                val userId = preferencesManager.getCurrentUserId()?.takeIf { it.isNotBlank() }
                    ?: "anonymous"
                val body = LocalRecommendationRequestBody(
                    userId = userId,
                    query = "Actividades e ideas destacadas en $placeId",
                    limit = 10,
                    candidates = listOf(
                        LocalRecommendationCandidateBody(
                            id = placeId,
                            name = placeId,
                            category = "destination",
                            price = 0.0,
                            contentText = placeId,
                        ),
                        LocalRecommendationCandidateBody(
                            id = "${placeId}_explore",
                            name = "Explorar cerca de $placeId",
                            category = "cultural",
                            price = 0.0,
                            contentText = "ideas de viaje y experiencias",
                        ),
                    ),
                )
                val response = voyagerAiRepository.postLocalRecommendations(body)
                if (response.isSuccessful) {
                    _payload.value = response.body()?.string()?.take(12_000)
                        ?: "(Respuesta vacía)"
                } else {
                    _error.value = response.errorBody()?.string()?.take(2_000)
                        ?: "HTTP ${response.code()}"
                }
            }.onFailure {
                _error.value = it.message ?: "Error de red"
            }
            _isLoading.value = false
        }
    }
}
