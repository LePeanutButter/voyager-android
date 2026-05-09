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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.model.TripStatus
import com.voyager.tourism.presentation.ui.trip.spanishLabel
import com.voyager.tourism.presentation.viewmodel.AuthViewModel
import com.voyager.tourism.presentation.viewmodel.DashboardInsightsViewModel
import com.voyager.tourism.presentation.viewmodel.TripViewModel
import java.util.Calendar

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
    onTrendingDestinationClick: (destinationId: String?) -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
            Row {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Filled.Settings, contentDescription = "Configuración")
                }
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onPlanTripClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Nuevo plan")
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

        // Quick Actions (alineado con chips del header web: recomendaciones, comunidad, crear plan)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onRecommendationsClick,
                modifier = Modifier.weight(1f),
            ) {
                Text("Recomendaciones")
            }
            Button(
                onClick = onSharedActivitiesClick,
                modifier = Modifier.weight(1f),
            ) {
                Text("Comunidad")
            }
            Button(
                onClick = onPlanTripClick,
                modifier = Modifier.weight(1f),
            ) {
                Text("Crear plan")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onAiAssistantClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Asistente IA")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DashboardAiInsightsSection(
            insightsViewModel = insightsViewModel,
            onTrendingDestinationClick = onTrendingDestinationClick,
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trips.take(5)) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { onTripClick(trip.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardAiInsightsSection(
    insightsViewModel: DashboardInsightsViewModel,
    onTrendingDestinationClick: (String?) -> Unit,
) {
    val trending by insightsViewModel.trending.collectAsState()
    val trendingErr by insightsViewModel.trendingError.collectAsState()
    val trendingLoad by insightsViewModel.trendingLoading.collectAsState()
    val weekly by insightsViewModel.weeklyLines.collectAsState()
    val weeklyErr by insightsViewModel.weeklyError.collectAsState()
    val season by insightsViewModel.seasonalityLines.collectAsState()
    val seasonErr by insightsViewModel.seasonalityError.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen inteligente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Tendencias, digest semanal y estacionalidad (IA).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tendencias",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            when {
                trendingLoad -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
                trendingErr != null -> {
                    Text(
                        text = trendingErr!!,
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
                        val line = buildString {
                            append(dest.name)
                            if (dest.country.isNotBlank()) {
                                append(" · ")
                                append(dest.country)
                            }
                        }
                        Text(
                            text = "· $line",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { onTrendingDestinationClick(dest.id) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Digest semanal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            when {
                weeklyErr != null -> {
                    Text(
                        text = weeklyErr!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                weekly.isEmpty() -> {
                    Text(
                        text = "Sin highlights esta semana.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    weekly.forEach { line ->
                        Text(
                            text = "· $line",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Estacionalidad",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            when {
                seasonErr != null -> {
                    Text(
                        text = seasonErr!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                season.isEmpty() -> {
                    Text(
                        text = "Sin panorama estacional por ahora.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    season.forEach { line ->
                        Text(
                            text = "· $line",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
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
