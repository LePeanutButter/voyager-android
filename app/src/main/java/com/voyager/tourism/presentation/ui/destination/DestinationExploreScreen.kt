package com.voyager.tourism.presentation.ui.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.data.catalog.CatalogActivityRow
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.trip.tripMatchesDestinationLabel
import com.voyager.tourism.presentation.viewmodel.DestinationExploreViewModel
import com.voyager.tourism.presentation.viewmodel.TripViewModel
import com.voyager.tourism.presentation.ui.trip.spanishLabel

data class DestinationExploreCallbacks(
    val onBack: () -> Unit,
    val onTripClick: (String) -> Unit,
    val onCreatePlanHint: (String) -> Unit
)

/**
 * Pantalla equivalente a [voyager-web-client/src/pages/DestinationExplore/DestinationExplorePage.jsx]:
 * catálogo geo, ranking IA sobre actividades, y planes del usuario que coinciden con el destino.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationExploreScreen(
    locRaw: String,
    countryRaw: String,
    destIdRaw: String,
    userId: String,
    callbacks: DestinationExploreCallbacks,
    exploreViewModel: DestinationExploreViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel(),
) {
    val destinationLabel by exploreViewModel.destinationLabel.collectAsState()
    val catalogDestId by exploreViewModel.catalogDestId.collectAsState()
    val catalogLoading by exploreViewModel.catalogLoading.collectAsState()
    val catalogError by exploreViewModel.catalogError.collectAsState()
    val activities by exploreViewModel.activities.collectAsState()
    val rankLoading by exploreViewModel.rankLoading.collectAsState()
    val rankError by exploreViewModel.rankError.collectAsState()
    val ranked by exploreViewModel.ranked.collectAsState()
    val trips by tripViewModel.trips.collectAsState()

    LaunchedEffect(locRaw, countryRaw, destIdRaw) {
        exploreViewModel.loadExplore(locRaw, countryRaw, destIdRaw)
    }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) tripViewModel.loadTrips(userId)
    }

    val matchingTrips = trips.filter { tripMatchesDestinationLabel(it, destinationLabel) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explorar destino", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = callbacks.onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                DestinationHeaderSection(
                    destinationLabel = destinationLabel,
                    catalogDestId = catalogDestId
                )
            }

            item {
                AiRankingSection(
                    rankLoading = rankLoading,
                    rankError = rankError,
                    ranked = activities,
                    activities = activities,
                    onRankCatalog = { exploreViewModel.rankCatalog() }
                )
            }

            item {
                CatalogActivitiesSection(
                    catalogLoading = catalogLoading,
                    catalogError = catalogError,
                    activities = activities
                )
            }

            items(activities, key = { it.id }) { act ->
                CatalogActivityCard(
                    activity = act,
                    destinationLabel = destinationLabel,
                    onCreatePlan = { hint -> callbacks.onCreatePlanHint(hint) },
                )
            }

            item {
                MatchingTripsSection(
                    matchingTrips = matchingTrips,
                    onTripClick = callbacks.onTripClick
                )
            }
        }
    }
}

@Composable
private fun DestinationHeaderSection(
    destinationLabel: String,
    catalogDestId: String,
) {
    Text(
        destinationLabel,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
    )
    if (catalogDestId.isNotBlank()) {
        Text(
            "Referencia catálogo / tendencias: $catalogDestId",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Text(
        "Mismo enfoque que en el web: catálogo de actividades y ranking con IA según tu perfil.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AiRankingSection(
    rankLoading: Boolean,
    rankError: String?,
    ranked: List<CatalogActivityRow>,
    activities: List<CatalogActivityRow>,
    onRankCatalog: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.SmartToy, contentDescription = null, modifier = Modifier.size(22.dp))
            Text("Ordenar catálogo con IA", style = MaterialTheme.typography.titleMedium)
        }
        Button(
            onClick = onRankCatalog,
            enabled = !rankLoading && activities.isNotEmpty(),
        ) {
            if (rankLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Rankear")
            }
        }
    }
    rankError?.let { err ->
        Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    if (ranked.isNotEmpty()) {
        ranked.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(item.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    if (item.description.isNotBlank()) {
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogActivitiesSection(
    catalogLoading: Boolean,
    catalogError: String?,
    activities: List<CatalogActivityRow>,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Map, contentDescription = null)
        Text("Actividades del catálogo", style = MaterialTheme.typography.titleMedium)
    }
    when {
        catalogLoading -> {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator(modifier = Modifier.size(28.dp)) }
        }
        catalogError != null -> {
            Text(catalogError!!, color = MaterialTheme.colorScheme.error)
        }
        activities.isEmpty() -> {
            Text(
                "Sin actividades en catálogo para esta zona. Prueba otro destino o más tarde.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MatchingTripsSection(
    matchingTrips: List<Trip>,
    onTripClick: (String) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Star, contentDescription = null)
        Text("Tus planes hacia este destino", style = MaterialTheme.typography.titleMedium)
    }
    if (matchingTrips.isEmpty()) {
        Text(
            "No hay planes que coincidan. Crea uno con una idea del listado.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        matchingTrips.forEach { trip ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(trip.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            trip.destination.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(trip.status.spanishLabel(), style = MaterialTheme.typography.labelSmall)
                    TextButton(onClick = { onTripClick(trip.id) }) { Text("Ver plan") }
                }
            }
        }
    }
}

@Composable
private fun CatalogActivityCard(
    activity: CatalogActivityRow,
    destinationLabel: String,
    onCreatePlan: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(activity.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (activity.description.isNotBlank()) {
                Text(
                    activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(
                onClick = {
                    val hint = "$destinationLabel · ${activity.name}".trim()
                    onCreatePlan(hint)
                },
            ) {
                Text("Crear plan con esta idea")
            }
        }
    }
}
