package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.catalog.CatalogActivityRow
import com.voyager.tourism.data.destination.buildDestinationExploreLabel
import com.voyager.tourism.data.destination.resolveDestinationHint
import com.voyager.tourism.data.dto.ActivityDto
import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.dto.LocalRecommendationCandidateBody
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.localai.LocalRecommendationParsers
import com.voyager.tourism.data.localai.ParsedLocalRecommendationItem
import com.voyager.tourism.domain.repository.CatalogRepository
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
 * Exploración de destino: catálogo geo + ranking IA, alineado con [DestinationExplorePage.jsx].
 */
@HiltViewModel
class DestinationExploreViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val voyagerAiRepository: VoyagerAiRepository,
    private val preferencesManager: PreferencesManager,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _destinationLabel = MutableStateFlow("")
    val destinationLabel: StateFlow<String> = _destinationLabel.asStateFlow()

    private val _catalogDestId = MutableStateFlow("")
    val catalogDestId: StateFlow<String> = _catalogDestId.asStateFlow()

    private val _catalogLoading = MutableStateFlow(false)
    val catalogLoading: StateFlow<Boolean> = _catalogLoading.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError: StateFlow<String?> = _catalogError.asStateFlow()

    private val _activities = MutableStateFlow<List<CatalogActivityRow>>(emptyList())
    val activities: StateFlow<List<CatalogActivityRow>> = _activities.asStateFlow()

    private val _rankLoading = MutableStateFlow(false)
    val rankLoading: StateFlow<Boolean> = _rankLoading.asStateFlow()

    private val _rankError = MutableStateFlow<String?>(null)
    val rankError: StateFlow<String?> = _rankError.asStateFlow()

    private val _ranked = MutableStateFlow<List<ParsedLocalRecommendationItem>>(emptyList())
    val ranked: StateFlow<List<ParsedLocalRecommendationItem>> = _ranked.asStateFlow()

    fun loadExplore(locRaw: String, countryRaw: String, destIdRaw: String) {
        viewModelScope.launch {
            val label = buildDestinationExploreLabel(locRaw, countryRaw)
            _destinationLabel.value = label
            _catalogDestId.value = destIdRaw.trim()
            _catalogLoading.value = true
            _catalogError.value = null
            _activities.value = emptyList()
            _ranked.value = emptyList()
            _rankError.value = null
            runCatching {
                val hint = resolveDestinationHint(label)
                val res = withContext(dispatchers.io) {
                    catalogRepository.activities(
                        latitude = hint.lat,
                        longitude = hint.lng,
                        radius = 40.0,
                        radiusUnit = "KM",
                    )
                }
                _activities.value = res.data.orEmpty().mapNotNull { it.toCatalogRow() }
            }.onFailure {
                _catalogError.value = it.message ?: "Error de catálogo"
            }
            _catalogLoading.value = false
        }
    }

    fun rankCatalog() {
        viewModelScope.launch {
            val acts = _activities.value
            if (acts.isEmpty()) {
                _rankError.value = "Carga el catálogo antes de rankear."
                return@launch
            }
            val userId = preferencesManager.getCurrentUserId()?.takeIf { it.isNotBlank() }
            if (userId == null) {
                _rankError.value = "Inicia sesión para obtener recomendaciones priorizadas con IA."
                return@launch
            }
            _rankLoading.value = true
            _rankError.value = null
            _ranked.value = emptyList()
            runCatching {
                val label = _destinationLabel.value
                val body = LocalRecommendationRequestBody(
                    userId = userId,
                    query = "Qué hacer en $label: ordena por relevancia para un viajero",
                    limit = 5,
                    candidates = acts.map {
                        LocalRecommendationCandidateBody(
                            id = it.id,
                            name = it.name,
                            category = "catalog_activity",
                            price = 0.0,
                            contentText = it.description.ifBlank { it.name },
                        )
                    },
                )
                val res = withContext(dispatchers.io) { voyagerAiRepository.postLocalRecommendations(body) }
                if (res.isSuccessful) {
                    val rawJson = res.body()?.toString() ?: ""
                    _ranked.value = LocalRecommendationParsers.parseItems(rawJson)
                } else {
                    _rankError.value = res.errorBody()?.string()?.ifBlank { "HTTP ${res.code()}" } ?: "HTTP ${res.code()}"
                }
            }.onFailure {
                _rankError.value = it.message ?: "Error de red"
            }
            _rankLoading.value = false
        }
    }
}

private fun ActivityDto.toCatalogRow(): CatalogActivityRow? {
    val nameValue = name.trim()
    if (nameValue.isBlank()) return null
    return CatalogActivityRow(
        id = id,
        name = nameValue,
        description = shortDescription?.trim().orEmpty().ifBlank { nameValue },
    )
}
