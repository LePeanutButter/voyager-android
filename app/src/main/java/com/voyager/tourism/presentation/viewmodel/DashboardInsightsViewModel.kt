package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dashboard.AiDashboardParsers
import com.voyager.tourism.data.dashboard.ParsedDigestRow
import com.voyager.tourism.data.dashboard.ParsedSeasonalityRow
import com.voyager.tourism.data.dashboard.ParsedTrendingDestination
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Panel lateral de IA del dashboard web: [aiService.getTrendsDashboard], [getWeeklyTrendsDigest], [getSeasonalityOverview].
 */
@HiltViewModel
class DashboardInsightsViewModel @Inject constructor(
    private val voyagerAi: VoyagerAiRepository,
) : ViewModel() {

    private val _trending = MutableStateFlow<List<ParsedTrendingDestination>>(emptyList())
    val trending: StateFlow<List<ParsedTrendingDestination>> = _trending.asStateFlow()
    private val _trendingError = MutableStateFlow<String?>(null)
    val trendingError: StateFlow<String?> = _trendingError.asStateFlow()
    private val _trendingLoading = MutableStateFlow(false)
    val trendingLoading: StateFlow<Boolean> = _trendingLoading.asStateFlow()

    private val _weeklyRows = MutableStateFlow<List<ParsedDigestRow>>(emptyList())
    val weeklyRows: StateFlow<List<ParsedDigestRow>> = _weeklyRows.asStateFlow()
    private val _weeklyError = MutableStateFlow<String?>(null)
    val weeklyError: StateFlow<String?> = _weeklyError.asStateFlow()

    private val _seasonalityRows = MutableStateFlow<List<ParsedSeasonalityRow>>(emptyList())
    val seasonalityRows: StateFlow<List<ParsedSeasonalityRow>> = _seasonalityRows.asStateFlow()
    private val _seasonalityError = MutableStateFlow<String?>(null)
    val seasonalityError: StateFlow<String?> = _seasonalityError.asStateFlow()

    fun refreshInsights() {
        viewModelScope.launch {
            launch { loadTrending() }
            launch { loadWeekly() }
            launch { loadSeasonality() }
        }
    }

    private suspend fun loadTrending() {
        _trendingLoading.value = true
        _trendingError.value = null
        try {
            val res = withContext(Dispatchers.IO) { voyagerAi.getTrendsDashboard() }
            if (!res.isSuccessful) {
                _trendingError.value = "No se pudo cargar el panel de tendencias (servicio de IA)."
                _trending.value = emptyList()
                return
            }
            val body = res.body()?.string().orEmpty()
            _trending.value = AiDashboardParsers.parseTrendsDashboard(body)
        } catch (e: Exception) {
            _trendingError.value = e.message ?: "Error de tendencias"
            _trending.value = emptyList()
        } finally {
            _trendingLoading.value = false
        }
    }

    private suspend fun loadWeekly() {
        _weeklyError.value = null
        try {
            val res = withContext(Dispatchers.IO) { voyagerAi.getWeeklyDigest() }
            if (!res.isSuccessful) {
                _weeklyError.value = "Digest semanal no disponible."
                _weeklyRows.value = emptyList()
                return
            }
            val body = res.body()?.string().orEmpty()
            _weeklyRows.value = AiDashboardParsers.parseWeeklyDigestRows(body)
        } catch (e: Exception) {
            _weeklyError.value = e.message ?: "Digest no disponible"
            _weeklyRows.value = emptyList()
        }
    }

    private suspend fun loadSeasonality() {
        _seasonalityError.value = null
        try {
            val res = withContext(Dispatchers.IO) { voyagerAi.getSeasonalityOverview(null) }
            if (!res.isSuccessful) {
                _seasonalityError.value = "Panorama estacional no disponible."
                _seasonalityRows.value = emptyList()
                return
            }
            val body = res.body()?.string().orEmpty()
            _seasonalityRows.value = AiDashboardParsers.parseSeasonalityRows(body)
        } catch (e: Exception) {
            _seasonalityError.value = e.message ?: "Estacionalidad no disponible"
            _seasonalityRows.value = emptyList()
        }
    }
}
