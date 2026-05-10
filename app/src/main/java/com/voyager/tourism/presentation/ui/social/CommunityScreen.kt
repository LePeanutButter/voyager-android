package com.voyager.tourism.presentation.ui.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.domain.model.TravelerConnection
import com.voyager.tourism.presentation.viewmodel.CommunityTab
import com.voyager.tourism.presentation.viewmodel.CommunityViewModel
import com.voyager.tourism.presentation.viewmodel.DiscoverMatchRow
import java.util.Calendar
import kotlin.math.roundToInt

data class CommunityPlanData(
    val plans: List<TravelPlanDto>,
    val selectedPlanId: String,
    val onPlanSelected: (String) -> Unit
)

data class CommunityDiscoverData(
    val discoverLoading: Boolean,
    val rows: List<DiscoverMatchRow>,
    val highlights: List<DiscoverMatchRow>,
    val refreshCooldownSec: Int,
    val refreshNotice: String,
    val onRefresh: () -> Unit,
    val onConnect: (Long) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    onBack: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingRemove by remember { mutableStateOf<TravelerConnection?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadInitial()
    }

    LaunchedEffect(state.infoMessage) {
        val msg = state.infoMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearInfoMessage()
    }

    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        viewModel.clearError()
    }

    pendingRemove?.let { conn ->
        val label = connectionDisplayName(conn)
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text("Eliminar conexión") },
            text = { Text("¿Seguro que quieres eliminar la conexión con $label?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeConnection(conn.connectionId)
                        pendingRemove = null
                    },
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemove = null }) { Text("Cancelar") }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Comunidad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = state.activeTab.ordinal) {
                Tab(
                    selected = state.activeTab == CommunityTab.CONNECTIONS,
                    onClick = { viewModel.selectTab(CommunityTab.CONNECTIONS) },
                    text = { Text("Conexiones") },
                )
                Tab(
                    selected = state.activeTab == CommunityTab.REQUESTS,
                    onClick = { viewModel.selectTab(CommunityTab.REQUESTS) },
                    text = { Text("Solicitudes") },
                )
                Tab(
                    selected = state.activeTab == CommunityTab.DISCOVER,
                    onClick = { viewModel.selectTab(CommunityTab.DISCOVER) },
                    text = { Text("Descubrir") },
                )
            }

            when (state.activeTab) {
                CommunityTab.CONNECTIONS -> ConnectionsTabContent(
                    isLoading = state.isLoadingHeader,
                    connections = state.connections,
                    onRemoveClick = { pendingRemove = it },
                )
                CommunityTab.REQUESTS -> RequestsTabContent(
                    isLoading = state.isLoadingHeader,
                    requests = state.pendingRequests,
                    onAccept = viewModel::acceptRequest,
                    onReject = viewModel::rejectRequest,
                )
                CommunityTab.DISCOVER -> DiscoverTabContent(
                    planData = CommunityPlanData(
                        plans = state.myPlans,
                        selectedPlanId = state.selectedPlanId,
                        onPlanSelected = viewModel::setSelectedPlan
                    ),
                    discoverData = CommunityDiscoverData(
                        discoverLoading = state.discoverLoading,
                        rows = state.discoverRows,
                        highlights = state.aiHighlightRows,
                        refreshCooldownSec = state.refreshCooldownSec,
                        refreshNotice = state.refreshNotice,
                        onRefresh = viewModel::refreshDiscoverManual,
                        onConnect = viewModel::sendDiscoverConnect
                    )
                )
            }
        }
    }
}

@Composable
private fun ConnectionsTabContent(
    isLoading: Boolean,
    connections: List<TravelerConnection>,
    onRemoveClick: (TravelerConnection) -> Unit,
) {
    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
        connections.isEmpty() -> EmptyState(
            title = "Aún no tienes conexiones",
            subtitle = "Descubre viajeros compatibles en la pestaña Descubrir.",
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(connections, key = { it.connectionId }) { conn ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                connectionDisplayName(conn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "@${conn.username} · ${conn.status}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { onRemoveClick(conn) }) {
                            Text("Quitar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestsTabContent(
    isLoading: Boolean,
    requests: List<ConnectionRequestDto>,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit,
) {
    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
        requests.isEmpty() -> EmptyState(
            title = "No hay solicitudes pendientes",
            subtitle = "Cuando alguien te envíe una solicitud, aparecerá aquí.",
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(requests, key = { it.id }) { req ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            req.requesterName?.trim()?.ifBlank { null } ?: "Solicitud #${req.id}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (!req.message.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(req.message!!, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onAccept(req.id) }) { Text("Aceptar") }
                            OutlinedButton(onClick = { onReject(req.id) }) { Text("Rechazar") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverTabContent(
    planData: CommunityPlanData,
    discoverData: CommunityDiscoverData,
) {
    var planMenuOpen by remember { mutableStateOf(false) }
    val selectedPlan = planData.plans.find { it.id?.toString() == planData.selectedPlanId } ?: planData.plans.firstOrNull()
    val planButtonLabel = selectedPlan?.let { planTitle(it) } ?: "Elegir plan"

    if (planData.plans.isEmpty()) {
        EmptyState(
            title = "Crea un plan de viaje",
            subtitle = "Necesitas al menos un plan para buscar viajeros compatibles e IA.",
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { planMenuOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(planButtonLabel, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                }
                DropdownMenu(expanded = planMenuOpen, onDismissRequest = { planMenuOpen = false }) {
                    planData.plans.forEach { plan ->
                        val idStr = plan.id?.toString().orEmpty()
                        if (idStr.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text(planTitle(plan)) },
                                onClick = {
                                    planMenuOpen = false
                                    planData.onPlanSelected(idStr)
                                },
                            )
                        }
                    }
                }
            }
            IconButton(
                onClick = discoverData.onRefresh,
                enabled = discoverData.refreshCooldownSec <= 0 && !discoverData.discoverLoading,
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar descubrimiento")
            }
        }

        if (discoverData.refreshNotice.isNotBlank()) {
            Text(
                discoverData.refreshNotice,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (discoverData.refreshCooldownSec > 0) {
            Text(
                "Puedes refrescar en ${discoverData.refreshCooldownSec} segundos",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (discoverData.discoverLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return
        }

        DiscoverMatchesList(
            discoverData = discoverData
        )
    }
}

@Composable
private fun DiscoverMatchCard(
    row: DiscoverMatchRow,
    onConnect: (Long) -> Unit,
    emphasizeAi: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (emphasizeAi) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        matchDisplayName(row),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "@${row.username} · ${formatMatchSource(row.source)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "${formatScorePercent(row.compatibilityScore)}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            val dest = row.destinationLocation?.takeIf { it.isNotBlank() }
            val dates = listOfNotNull(row.travelStartDate, row.travelEndDate).filter { it.isNotBlank() }
            if (dest != null || dates.isNotEmpty() || !row.travelPlanTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                row.travelPlanTitle?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                dest?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (dates.isNotEmpty()) {
                    Text(dates.joinToString(" → "), style = MaterialTheme.typography.labelSmall)
                }
            }
            if (row.sharedDestinations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Coincidencias: ${row.sharedDestinations.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = { onConnect(row.userId) }) {
                    Text("Conectar")
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun planTitle(plan: TravelPlanDto): String {
    val t = plan.title?.trim().orEmpty()
    val d = plan.destinationLocation?.trim().orEmpty()
    return when {
        t.isNotEmpty() && d.isNotEmpty() -> "$t · $d"
        t.isNotEmpty() -> t
        d.isNotEmpty() -> d
        plan.id != null -> "Plan ${plan.id}"
        else -> "Plan"
    }
}

private fun connectionDisplayName(c: TravelerConnection): String {
    val full = listOfNotNull(c.firstName, c.lastName).joinToString(" ").trim()
    return full.ifBlank { c.username }
}

private fun matchDisplayName(r: DiscoverMatchRow): String {
    val full = "${r.firstName} ${r.lastName}".trim()
    return full.ifBlank { r.username }
}

private fun formatMatchSource(source: String): String = when (source) {
    "ai" -> "IA"
    "both" -> "IA + backend"
    else -> "Compatibilidad"
}

@Composable
private fun DiscoverMatchesList(
    discoverData: CommunityDiscoverData,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (discoverData.highlights.isNotEmpty()) {
            item {
                Text(
                    "Destacados IA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items(discoverData.highlights, key = { "h-${it.userId}" }) { row ->
                DiscoverMatchCard(row = row, onConnect = discoverData.onConnect, emphasizeAi = true)
            }
            item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
        }
        item {
            Text(
                "Compatibles y sugerencias",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (discoverData.rows.isEmpty()) {
            item {
                Text(
                    "No hay coincidencias para este plan. Prueba otro plan o actualiza más tarde.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(discoverData.rows, key = { it.userId }) { row ->
                DiscoverMatchCard(row = row, onConnect = discoverData.onConnect, emphasizeAi = row.isAiHighlight)
            }
        }
    }
}

private fun formatScorePercent(score: Double): Int =
    if (score in 0.0..1.0) (score * 100).roundToInt() else score.roundToInt().coerceIn(0, 9999)
