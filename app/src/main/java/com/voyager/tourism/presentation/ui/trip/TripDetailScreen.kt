package com.voyager.tourism.presentation.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.presentation.viewmodel.TripDetailViewModel

/**
 * Trip overview: metadata, description, and an embedded activity timeline for the given trip.
 *
 * @param tripId Identifier of the trip to load.
 * @param onBack Invoked when navigating back from the top bar.
 * @param viewModel Provides trip, activities, loading, and error state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onBack: () -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel(),
) {
    val trip by viewModel.trip.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(tripId) {
        viewModel.loadTrip(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.title ?: "Viaje") },
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
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                trip == null -> {
                    Text("No se encontró el viaje.")
                }
                else -> {
                    val t = trip!!
                    Text("Destino: ${t.destination.name}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        t.description.ifBlank { "Sin descripción" },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Presupuesto: ${t.budget} · Viajeros: ${t.travelers} · Estado: ${t.status.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ActivityTimelineScreen(
                        activities = activities,
                        onAddFirstActivity = { /* futuro: navegar a crear actividad */ },
                    )
                }
            }
        }
    }
}
