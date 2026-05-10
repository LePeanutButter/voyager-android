package com.voyager.tourism.presentation.ui.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import com.voyager.tourism.data.dashboard.ParsedDigestRow
import com.voyager.tourism.data.dashboard.ParsedSeasonalityRow
import com.voyager.tourism.data.dashboard.ParsedTrendingDestination
import com.voyager.tourism.data.destination.buildDestinationExploreLabel
import com.voyager.tourism.domain.trip.tripMatchesDestinationLabel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.model.TripStatus
import com.voyager.tourism.presentation.ui.trip.spanishLabel
import com.voyager.tourism.presentation.navigation.DestinationExploreParams
import com.voyager.tourism.presentation.viewmodel.AuthViewModel
import com.voyager.tourism.presentation.viewmodel.DashboardInsightsViewModel
import com.voyager.tourism.presentation.viewmodel.TripViewModel
import java.util.Calendar

private const val CREATE_PLAN_TEXT = "Crear plan"

/**
 * Main dashboard screen showing trip overview and quick actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("kotlin:S107")
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onTripClick: (String) -> Unit,
    onRecommendationsClick: () -> Unit,
    onSharedActivitiesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPlanTripClick: () -> Unit,
    onAiAssistantClick: () -> Unit,
    onOpenCalendar: () -> Unit = {},
    onCreatePlanWithDestinationHint: (String) -> Unit = {},
    onDestinationExplore: (DestinationExploreParams) -> Unit,
    onViewAllTrips: () -> Unit = {},
    viewModel: TripViewModel = hiltViewModel(),
    insightsViewModel: DashboardInsightsViewModel = hiltViewModel(),
) {
    val trips by viewModel.trips.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val tripError by viewModel.errorMessage.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser?.id) {
        val uid = currentUser?.id
        if (!uid.isNullOrBlank()) {
            viewModel.loadTrips(uid)
            insightsViewModel.refreshInsights()
        }
    }
    
    val firstName = currentUser?.firstName?.takeIf { it.isNotBlank() }
        ?: currentUser?.username?.takeIf { it.isNotBlank() }
        ?: "Viajero"

    val scroll = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            DashboardBottomNavigationBar(
                onRecommendationsClick = onRecommendationsClick,
                onCommunityClick = onSharedActivitiesClick,
                onCreatePlanClick = onPlanTripClick,
                onProfileClick = onProfileClick,
                onAiAssistantClick = onAiAssistantClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scroll)
                .padding(16.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${dashboardGreetingEs()},",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$firstName 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Aquí tienes un resumen de tu actividad de viaje",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(onClick = onOpenCalendar) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Calendario / cronograma")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        tripError?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!isLoading && trips.isNotEmpty()) {
            DashboardStatsRow(trips = trips)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        DashboardAiInsightsSection(
            insightsViewModel = insightsViewModel,
            trips = trips,
            onDestinationExplore = onDestinationExplore,
            onCreatePlanWithDestinationHint = onCreatePlanWithDestinationHint,
            onOpenTripDetail = onTripClick,
            onOpenAssistant = onAiAssistantClick,
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Planes recientes",
                style = MaterialTheme.typography.titleLarge,
            )
            if (trips.isNotEmpty()) {
                TextButton(onClick = onViewAllTrips) {
                    Text("Ver todos")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (trips.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Aún no tienes planes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Crea tu primer plan de viaje y comienza tu aventura.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onPlanTripClick) {
                        Text("Crear primer plan")
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                trips.take(5).forEach { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { onTripClick(trip.id) },
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun DashboardBottomNavigationBar(
    onRecommendationsClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onCreatePlanClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAiAssistantClick: () -> Unit,
) {
    val colors = NavigationBarItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
    )
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = onRecommendationsClick,
            icon = { Icon(Icons.Filled.Star, contentDescription = "Recomendaciones") },
            label = { },
            alwaysShowLabel = false,
            colors = colors,
        )
        NavigationBarItem(
            selected = false,
            onClick = onCommunityClick,
            icon = { Icon(Icons.Filled.Groups, contentDescription = "Comunidad") },
            label = { },
            alwaysShowLabel = false,
            colors = colors,
        )
        NavigationBarItem(
            selected = false,
            onClick = onCreatePlanClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = CREATE_PLAN_TEXT) },
            label = { },
            alwaysShowLabel = false,
            colors = colors,
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil") },
            label = { },
            alwaysShowLabel = false,
            colors = colors,
        )
        NavigationBarItem(
            selected = false,
            onClick = onAiAssistantClick,
            icon = { Icon(Icons.Filled.SmartToy, contentDescription = "Asistente IA") },
            label = { },
            alwaysShowLabel = false,
            colors = colors,
        )
    }
}

private sealed class InsightSheet {
    data class WeeklyDigest(val rows: List<ParsedDigestRow>) : InsightSheet()
    data class Seasonal(val rows: List<ParsedSeasonalityRow>) : InsightSheet()
}

private fun tripsMatchingDigest(trips: List<Trip>, row: ParsedDigestRow): List<Trip> {
    val id = row.destId?.trim().orEmpty()
    if (id.isNotBlank()) {
        val byId = trips.filter { it.destination.id.equals(id, ignoreCase = true) }
        if (byId.isNotEmpty()) return byId
    }
    val label = buildDestinationExploreLabel(row.exploreQuery, row.country.orEmpty())
    return trips.filter { tripMatchesDestinationLabel(it, label) }
}

private fun tripsMatchingSeasonality(trips: List<Trip>, row: ParsedSeasonalityRow): List<Trip> {
    val id = row.id?.trim().orEmpty()
    if (id.isNotBlank()) {
        val byId = trips.filter { it.destination.id.equals(id, ignoreCase = true) }
        if (byId.isNotEmpty()) return byId
    }
    val label = buildDestinationExploreLabel(row.title, "")
    return trips.filter { tripMatchesDestinationLabel(it, label) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardAiInsightsSection(
    insightsViewModel: DashboardInsightsViewModel,
    trips: List<Trip>,
    onDestinationExplore: (DestinationExploreParams) -> Unit,
    onCreatePlanWithDestinationHint: (String) -> Unit,
    onOpenTripDetail: (String) -> Unit,
    onOpenAssistant: () -> Unit,
) {
    val trending by insightsViewModel.trending.collectAsState()
    val trendingErr by insightsViewModel.trendingError.collectAsState()
    val trendingLoad by insightsViewModel.trendingLoading.collectAsState()
    val weeklyRows by insightsViewModel.weeklyRows.collectAsState()
    val weeklyErr by insightsViewModel.weeklyError.collectAsState()
    val seasonRows by insightsViewModel.seasonalityRows.collectAsState()
    val seasonErr by insightsViewModel.seasonalityError.collectAsState()

    fun openExploreFromDigest(row: ParsedDigestRow) {
        if (digestRowHasGeo(row)) {
            onDestinationExplore(
                DestinationExploreParams(
                    loc = row.exploreQuery,
                    country = row.country,
                    destId = row.destId,
                ),
            )
        } else {
            onOpenAssistant()
        }
    }

    fun openExploreFromSeasonality(row: ParsedSeasonalityRow) {
        onDestinationExplore(
            DestinationExploreParams(
                loc = row.title,
                country = null,
                destId = row.id,
            ),
        )
    }

    var sheet by remember { mutableStateOf<InsightSheet?>(null) }
    if (sheet != null) {
        val sheetContent = sheet!!
        ModalBottomSheet(onDismissRequest = { sheet = null }) {
            when (sheetContent) {
                is InsightSheet.WeeklyDigest -> WeeklyDigestFullSheet(
                    rows = sheetContent.rows,
                    trips = trips,
                    onDismiss = { sheet = null },
                    onExplore = { row ->
                        openExploreFromDigest(row)
                        sheet = null
                    },
                    onCreatePlan = { hint ->
                        onCreatePlanWithDestinationHint(hint)
                        sheet = null
                    },
                    onOpenTrip = { id ->
                        onOpenTripDetail(id)
                        sheet = null
                    },
                )
                is InsightSheet.Seasonal -> SeasonalityFullSheet(
                    rows = sheetContent.rows,
                    trips = trips,
                    onDismiss = { sheet = null },
                    onExploreIdeas = { row ->
                        openExploreFromSeasonality(row)
                        sheet = null
                    },
                    onCreatePlan = { hint ->
                        onCreatePlanWithDestinationHint(hint)
                        sheet = null
                    },
                    onOpenTrip = { id ->
                        onOpenTripDetail(id)
                        sheet = null
                    },
                )
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Resumen inteligente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Tendencias, digest y estacionalidad (mismo flujo que el panel web).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            InsightSubsectionHeader(
                icon = { Icon(Icons.Filled.Public, contentDescription = null) },
                title = "Tendencias",
                subtitle = "Destinos emergentes",
            )
            TrendingSection(
                trending = trending,
                trendingErr = trendingErr,
                trendingLoad = trendingLoad,
                onDestinationExplore = onDestinationExplore
            )

            Spacer(modifier = Modifier.height(16.dp))
            InsightSubsectionHeader(
                icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                title = "Digest semanal",
                subtitle = "Micro-tendencias",
            )
            WeeklyDigestSection(
                weeklyRows = weeklyRows,
                weeklyErr = weeklyErr,
                onOpenExploreFromDigest = { openExploreFromDigest(it) },
                onShowFullDigest = { sheet = InsightSheet.WeeklyDigest(weeklyRows) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            InsightSubsectionHeader(
                icon = { Icon(Icons.Filled.WbSunny, contentDescription = null) },
                title = "Estacionalidad",
                subtitle = "Patrones por destino",
            )
            SeasonalitySection(
                seasonRows = seasonRows,
                seasonErr = seasonErr,
                onOpenExploreFromSeasonality = { openExploreFromSeasonality(it) },
                onShowFullSeasonality = { sheet = InsightSheet.Seasonal(seasonRows) }
            )
        }
    }
}

@Composable
private fun TrendingSection(
    trending: List<ParsedTrendingDestination>,
    trendingErr: String?,
    trendingLoad: Boolean,
    onDestinationExplore: (DestinationExploreParams) -> Unit,
) {
    when {
        trendingLoad -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
        trendingErr != null -> {
            Text(
                text = trendingErr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        trending.isEmpty() -> {
            Text(
                text = "Sin datos de tendencias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        else -> {
            trending.forEach { dest ->
                TrendingDestinationRow(
                    dest = dest,
                    onOpen = {
                        onDestinationExplore(
                            DestinationExploreParams(
                                loc = dest.name,
                                country = dest.country.takeIf { it.isNotBlank() },
                                destId = dest.id,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun WeeklyDigestSection(
    weeklyRows: List<ParsedDigestRow>,
    weeklyErr: String?,
    onOpenExploreFromDigest: (ParsedDigestRow) -> Unit,
    onShowFullDigest: () -> Unit,
) {
    when {
        weeklyErr != null -> {
            Text(
                text = weeklyErr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        weeklyRows.isEmpty() -> {
            Text(
                text = "Sin highlights esta semana.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        else -> {
            weeklyRows.take(2).forEach { row ->
                DigestPreviewRow(
                    row = row,
                    onClick = { onOpenExploreFromDigest(row) },
                )
            }
            TextButton(onClick = onShowFullDigest) {
                Text("Ver digest completo")
            }
        }
    }
}

@Composable
private fun SeasonalitySection(
    seasonRows: List<ParsedSeasonalityRow>,
    seasonErr: String?,
    onOpenExploreFromSeasonality: (ParsedSeasonalityRow) -> Unit,
    onShowFullSeasonality: () -> Unit,
) {
    when {
        seasonErr != null -> {
            Text(
                text = seasonErr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        seasonRows.isEmpty() -> {
            Text(
                text = "Sin panorama estacional por ahora.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        else -> {
            seasonRows.take(2).forEach { row ->
                SeasonalityPreviewRow(
                    row = row,
                    onExploreIdeas = { onOpenExploreFromSeasonality(row) },
                )
            }
            TextButton(onClick = onShowFullSeasonality) {
                Text("Ver panorama completo")
            }
        }
    }
}

private fun digestRowHasGeo(row: ParsedDigestRow): Boolean =
    row.exploreQuery.isNotBlank() || !row.country.isNullOrBlank()

@Composable
private fun DigestPreviewRow(
    row: ParsedDigestRow,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(row.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            if (row.subtitle.isNotBlank()) {
                Text(row.subtitle, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        },
        trailingContent = {
            TextButton(onClick = onClick) {
                Text("Abrir")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
    )
}

@Composable
private fun SeasonalityPreviewRow(
    row: ParsedSeasonalityRow,
    onExploreIdeas: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(row.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(row.subtitle, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            FilledTonalButton(onClick = onExploreIdeas) {
                Text("Ideas")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExploreIdeas),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
    )
}

@Composable
private fun WeeklyDigestFullSheet(
    rows: List<ParsedDigestRow>,
    trips: List<Trip>,
    onDismiss: () -> Unit,
    onExplore: (ParsedDigestRow) -> Unit,
    onCreatePlan: (String) -> Unit,
    onOpenTrip: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Digest semanal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        rows.forEach { row ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(row.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    if (row.subtitle.isNotBlank()) {
                        Text(row.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(onClick = { onExplore(row) }) {
                            Text("Ver ideas")
                        }
                        TextButton(onClick = { onCreatePlan(row.exploreQuery.ifBlank { row.title }) }) {
                            Text(CREATE_PLAN_TEXT)
                        }
                    }
                    val matches = tripsMatchingDigest(trips, row)
                    matches.take(2).forEach { trip ->
                        TextButton(onClick = { onOpenTrip(trip.id) }) {
                            Text("Ver plan: ${trip.title}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar")
        }
    }
}

@Composable
private fun SeasonalityFullSheet(
    rows: List<ParsedSeasonalityRow>,
    trips: List<Trip>,
    onDismiss: () -> Unit,
    onExploreIdeas: (ParsedSeasonalityRow) -> Unit,
    onCreatePlan: (String) -> Unit,
    onOpenTrip: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Estacionalidad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        rows.forEach { row ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(row.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(row.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(onClick = { onExploreIdeas(row) }) {
                            Text("Ver ideas para viajar")
                        }
                        TextButton(onClick = { onCreatePlan(row.title) }) {
                            Text(CREATE_PLAN_TEXT)
                        }
                    }
                    val matches = tripsMatchingSeasonality(trips, row)
                    matches.take(2).forEach { trip ->
                        TextButton(onClick = { onOpenTrip(trip.id) }) {
                            Text("Ver mi plan: ${trip.title}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar")
        }
    }
}

@Composable
private fun InsightSubsectionHeader(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleSmall) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.labelSmall) },
        leadingContent = icon,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TrendingDestinationRow(
    dest: ParsedTrendingDestination,
    onOpen: () -> Unit,
) {
    val initial = dest.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    ListItem(
        headlineContent = {
            Text(
                dest.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            if (dest.country.isNotBlank()) {
                Text(dest.country, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        initial,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        },
        trailingContent = {
            TextButton(onClick = onOpen) {
                Text("Ver")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
    )
}

/**
 * Compact card for a trip preview on the dashboard scroll list.
 */
@Composable
private fun TripCard(
    trip: com.voyager.tourism.domain.model.Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = trip.title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = trip.destination.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${trip.budget}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = trip.status.spanishLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun dashboardGreetingEs(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Buenos días"
        h < 18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

@Composable
private fun DashboardStatsRow(trips: List<Trip>) {
    val total = trips.size
    val upcoming = trips.count { it.status == TripStatus.PLANNING || it.status == TripStatus.ACTIVE || it.status == TripStatus.CONFIRMED }
    val completed = trips.count { it.status == TripStatus.COMPLETED }
    val destinations = trips.map { it.destination.name }.filter { it.isNotBlank() }.distinct().size

    val stats = listOf(
        Triple(total, "Planes totales", Icons.Filled.Flight),
        Triple(upcoming, "Próximos", Icons.Filled.CalendarMonth),
        Triple(completed, "Completados", Icons.Filled.Star),
        Triple(destinations, "Destinos", Icons.Filled.Public),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        stats.forEach { (value, label, icon) ->
            Card(
                modifier = Modifier.width(140.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "$value",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
