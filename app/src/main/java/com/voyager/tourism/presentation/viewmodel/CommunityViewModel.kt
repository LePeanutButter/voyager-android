package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.AiTravelerMatchDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.model.TravelerConnection
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import com.voyager.tourism.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/** Pestañas alineadas con [voyager-web-client/src/pages/Social/Social.jsx]. */
enum class CommunityTab {
    CONNECTIONS,
    REQUESTS,
    DISCOVER,
}

/**
 * Fila unificada de descubrimiento (backend compatible-travelers + IA matching/recommendations).
 */
data class DiscoverMatchRow(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val username: String,
    val compatibilityScore: Double,
    val travelPlanTitle: String?,
    val destinationLocation: String?,
    val travelStartDate: String?,
    val travelEndDate: String?,
    val sharedDestinations: List<String>,
    val source: String,
    val isAiHighlight: Boolean,
)

data class CommunityUiState(
    val activeTab: CommunityTab = CommunityTab.CONNECTIONS,
    val isLoadingHeader: Boolean = false,
    val error: String? = null,
    val connections: List<TravelerConnection> = emptyList(),
    val pendingRequests: List<ConnectionRequestDto> = emptyList(),
    val myPlans: List<TravelPlanDto> = emptyList(),
    val selectedPlanId: String = "",
    val discoverRows: List<DiscoverMatchRow> = emptyList(),
    val aiHighlightRows: List<DiscoverMatchRow> = emptyList(),
    val discoverLoading: Boolean = false,
    val refreshCooldownSec: Int = 0,
    val refreshNotice: String = "",
    val infoMessage: String? = null,
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val travelRepository: TravelRepository,
    private val voyagerAi: VoyagerAiRepository,
    private val supplementRepository: BackendSupplementRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val manualRefreshTimestamps = ArrayList<Long>()
    private var cooldownJob: Job? = null

    fun selectTab(tab: CommunityTab) {
        _uiState.update { it.copy(activeTab = tab, error = null) }
        when (tab) {
            CommunityTab.CONNECTIONS, CommunityTab.REQUESTS -> loadConnectionsAndRequests()
            CommunityTab.DISCOVER -> loadDiscoverPlans()
        }
    }

    fun loadInitial() {
        selectTab(CommunityTab.CONNECTIONS)
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setSelectedPlan(planId: String) {
        _uiState.update { it.copy(selectedPlanId = planId) }
        viewModelScope.launch { fetchDiscoverMatches(recordManualSuccess = false) }
    }

    fun refreshDiscoverManual() {
        val gate = checkDiscoverRefreshAllowed()
        if (!gate.first) {
            _uiState.update {
                it.copy(
                    refreshNotice = gate.second.orEmpty(),
                    refreshCooldownSec = max(1, ceil(gate.third / 1000.0).roundToInt()),
                )
            }
            beginCooldownFromMillis(gate.third)
            return
        }
        recordDiscoverManualRefresh()
        _uiState.update { it.copy(refreshNotice = "") }
        viewModelScope.launch { fetchDiscoverMatches(recordManualSuccess = true) }
    }

    private fun loadConnectionsAndRequests() = viewModelScope.launch {
        val uid = preferencesManager.getCurrentUserId()?.toLongOrNull()
        if (uid == null) {
            _uiState.update { it.copy(error = "Inicia sesión para ver la comunidad.", isLoadingHeader = false) }
            return@launch
        }
        _uiState.update { it.copy(isLoadingHeader = true, error = null) }
        runCatching {
            coroutineScope {
                val connsDeferred = async(Dispatchers.IO) {
                    socialRepository.getConnections(uid).getOrThrow()
                }
                val pendingDeferred = async(Dispatchers.IO) {
                    socialRepository.getPendingRequests("")
                }
                connsDeferred.await() to pendingDeferred.await()
            }
        }.onSuccess { (conns, pending) ->
            _uiState.update {
                it.copy(isLoadingHeader = false, connections = conns, pendingRequests = pending)
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoadingHeader = false, error = e.message ?: "Error al cargar datos") }
        }
    }

    private fun loadDiscoverPlans() = viewModelScope.launch {
        val uid = preferencesManager.getCurrentUserId()
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Inicia sesión para descubrir viajeros.") }
            return@launch
        }
        travelRepository.getUserTravelPlans(uid)
            .onSuccess { plans ->
                val firstId = plans.firstOrNull()?.id?.toString().orEmpty()
                _uiState.update {
                    val sel = if (it.selectedPlanId.isNotBlank() && plans.any { p -> p.id?.toString() == it.selectedPlanId }) {
                        it.selectedPlanId
                    } else {
                        firstId
                    }
                    it.copy(myPlans = plans, selectedPlanId = sel)
                }
                fetchDiscoverMatches(recordManualSuccess = false)
            }
            .onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "No se pudieron cargar tus planes") }
            }
    }

    private suspend fun fetchDiscoverMatches(recordManualSuccess: Boolean) {
        val uidStr = preferencesManager.getCurrentUserId()
        val userId = uidStr?.toLongOrNull() ?: return
        val plans = _uiState.value.myPlans
        if (plans.isEmpty()) {
            _uiState.update { it.copy(discoverRows = emptyList(), aiHighlightRows = emptyList(), discoverLoading = false) }
            return
        }
        val selected = _uiState.value.selectedPlanId.ifBlank { plans.first().id?.toString().orEmpty() }
        if (selected.isBlank()) return
        val planMeta = plans.find { it.id?.toString() == selected } ?: plans.first()
        val dest = planMeta.destinationLocation.orEmpty()
        val footprint = plans.mapNotNull { it.destinationLocation?.trim()?.takeIf { d -> d.isNotEmpty() } }.distinct()

        _uiState.update { it.copy(discoverLoading = true, error = null) }
        runCatching {
            coroutineScope {
                val compatDeferred: Deferred<List<TravelerMatchDto>> = async(Dispatchers.IO) {
                    runCatching {
                        socialRepository.getCompatibleTravelers(selected, "")
                    }.getOrDefault(emptyList<TravelerMatchDto>())
                }

                val footprintParam = if (footprint.isNotEmpty()) footprint.joinToString(",") else null
                val buddyDeferred: Deferred<List<AiTravelerMatchDto>> = async(Dispatchers.IO) {
                    runCatching {
                        val resp = voyagerAi.getTravelBuddyRecommendations(
                            userId = uidStr,
                            location = dest.ifBlank { null },
                            limit = 15,
                            seekerFootprint = footprintParam,
                        )
                        if (resp.isSuccessful) resp.body()?.matches.orEmpty() else emptyList<AiTravelerMatchDto>()
                    }.getOrDefault(emptyList<AiTravelerMatchDto>())
                }

                val compat = compatDeferred.await()
                val buddyBody = buddyDeferred.await()
                val merged = mergeDiscoveryMatches(compat, buddyBody, dest, userId)
                val onlyAi = merged.filter { it.source == "ai" || it.source == "both" }.take(4)
                    .map { it.copy(isAiHighlight = true) }
                val aiIds = onlyAi.map { it.userId }.toSet()
                val ranked = onlyAi + merged.filter { it.userId !in aiIds }

                _uiState.update {
                    it.copy(
                        discoverLoading = false,
                        aiHighlightRows = onlyAi,
                        discoverRows = ranked,
                    )
                }
            }
            if (recordManualSuccess) {
                beginManualRefreshCooldownFull()
            }
        }.onFailure { e ->
            _uiState.update {
                it.copy(discoverLoading = false, error = e.message ?: "No se pudieron cargar sugerencias")
            }
        }
    }

    fun sendDiscoverConnect(recipientId: Long) = viewModelScope.launch {
        runCatching {
            socialRepository.sendConnectionRequest(
                SendConnectionRequestDto(recipientId = recipientId, message = "¡Hola! Conectemos."),
                "",
            )
        }.onSuccess {
            _uiState.update { it.copy(infoMessage = "Solicitud de conexión enviada") }
            loadConnectionsAndRequests()
        }.onFailure { e ->
            _uiState.update { it.copy(error = e.message ?: "No se pudo enviar la solicitud") }
        }
    }

    fun acceptRequest(requestId: Long) = viewModelScope.launch {
        runCatching { socialRepository.acceptConnectionRequest(requestId.toString(), "") }
            .onSuccess { loadConnectionsAndRequests() }
            .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
    }

    fun rejectRequest(requestId: Long) = viewModelScope.launch {
        runCatching { socialRepository.rejectConnectionRequest(requestId.toString(), "") }
            .onSuccess { loadConnectionsAndRequests() }
            .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
    }

    fun removeConnection(connectionId: Long) = viewModelScope.launch {
        runCatching { supplementRepository.removeConnection(connectionId) }
            .onSuccess { resp ->
                if (resp.status == 200) {
                    _uiState.update { it.copy(infoMessage = "Conexión eliminada") }
                    loadConnectionsAndRequests()
                } else {
                    _uiState.update { it.copy(error = resp.message.ifBlank { "No se pudo eliminar" }) }
                }
            }
            .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
    }

    private companion object {
        const val DISCOVER_REFRESH_COOLDOWN_MS = 60_000L
        const val DISCOVER_REFRESH_WINDOW_MS = 60_000L
        const val DISCOVER_REFRESH_MAX_PER_WINDOW = 6
    }

    private fun checkDiscoverRefreshAllowed(): Triple<Boolean, String?, Long> {
        val now = System.currentTimeMillis()
        val windowStart = now - DISCOVER_REFRESH_WINDOW_MS
        while (manualRefreshTimestamps.isNotEmpty() && manualRefreshTimestamps.first() < windowStart) {
            manualRefreshTimestamps.removeAt(0)
        }
        if (manualRefreshTimestamps.size >= DISCOVER_REFRESH_MAX_PER_WINDOW) {
            val oldest = manualRefreshTimestamps.first()
            val retry = max(0L, oldest + DISCOVER_REFRESH_WINDOW_MS - now)
            return Triple(false, "Has alcanzado el límite de actualizaciones. Vuelve a intentar en unos segundos.", retry)
        }
        val last = manualRefreshTimestamps.lastOrNull()
        if (last != null && now - last < DISCOVER_REFRESH_COOLDOWN_MS) {
            val retry = max(0L, DISCOVER_REFRESH_COOLDOWN_MS - (now - last))
            return Triple(false, "Por seguridad, espera antes de volver a actualizar.", retry)
        }
        return Triple(true, null, 0L)
    }

    private fun recordDiscoverManualRefresh() {
        manualRefreshTimestamps.add(System.currentTimeMillis())
    }

    private fun beginManualRefreshCooldownFull() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.update { it.copy(refreshCooldownSec = 60) }
            repeat(60) {
                if (BuildConfig.DEBUG) {
                    // In debug builds keep the per-second ticking for easier observation
                    kotlinx.coroutines.delay(1000)
                } else {
                    // In release builds, update UI without artificial long-running delays
                    // (keeps responsiveness and avoids blocking test runners)
                    kotlinx.coroutines.yield()
                }
                _uiState.update { s -> s.copy(refreshCooldownSec = max(0, s.refreshCooldownSec - 1)) }
            }
        }
    }

    private fun beginCooldownFromMillis(remainingMs: Long) {
        val sec = max(1, ceil(remainingMs / 1000.0).roundToInt())
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var left = sec
            while (left > 0) {
                _uiState.update { it.copy(refreshCooldownSec = left) }
                if (BuildConfig.DEBUG) kotlinx.coroutines.delay(1000) else kotlinx.coroutines.yield()
                left--
            }
            _uiState.update { it.copy(refreshCooldownSec = 0) }
        }
    }
}

private fun mergeDiscoveryMatches(
    compat: List<TravelerMatchDto>,
    buddyMatches: List<AiTravelerMatchDto>,
    planDestination: String,
    currentUserId: Long,
): List<DiscoverMatchRow> {
    val byId = LinkedHashMap<Long, DiscoverMatchRow>()
    for (m in compat) {
        if (m.userId == currentUserId) continue
        val shared = emptyList<String>()
        byId[m.userId] = DiscoverMatchRow(
            userId = m.userId,
            firstName = m.firstName,
            lastName = m.lastName,
            username = m.username,
            compatibilityScore = m.compatibilityScore ?: 0.0,
            travelPlanTitle = m.travelPlanTitle,
            destinationLocation = m.destinationLocation,
            travelStartDate = m.travelStartDate,
            travelEndDate = m.travelEndDate,
            sharedDestinations = shared,
            source = "backend",
            isAiHighlight = false,
        )
    }
    for (r in buddyMatches) {
        val uid = r.userId.toLongOrNull() ?: continue
        if (uid == currentUserId) continue
        val existing = byId[uid]
        val score = (r.compatibilityScore / 100.0).coerceIn(0.0, 1.0)
        val parts = r.name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val firstName = parts.firstOrNull().orEmpty().ifBlank { "Viajero" }
        val lastName = parts.drop(1).joinToString(" ")
        val shared = r.sharedDestinations.ifEmpty { r.commonPreferences }
        val username = r.userId
        if (existing != null) {
            val newSource = if (existing.source == "backend") "both" else existing.source
            byId[uid] = existing.copy(
                compatibilityScore = max(existing.compatibilityScore, score),
                source = newSource,
                sharedDestinations = (existing.sharedDestinations + shared).distinct(),
            )
        } else {
            byId[uid] = DiscoverMatchRow(
                userId = uid,
                firstName = firstName,
                lastName = lastName,
                username = username,
                compatibilityScore = score,
                travelPlanTitle = null,
                destinationLocation = planDestination.ifBlank { null },
                travelStartDate = null,
                travelEndDate = null,
                sharedDestinations = shared,
                source = "ai",
                isAiHighlight = false,
            )
        }
    }
    return byId.values.sortedByDescending { it.compatibilityScore }
}
