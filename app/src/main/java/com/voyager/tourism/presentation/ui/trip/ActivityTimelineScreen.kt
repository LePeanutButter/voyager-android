package com.voyager.tourism.presentation.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voyager.tourism.data.dto.TravelPlanActivityDto

@Composable
fun ActivityTimelineScreen(
    activities: List<TravelPlanActivityDto>,
    onAddFirstActivity: () -> Unit
) {
    val ordered = activities.sortedBy { it.startTime }

    if (ordered.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("No hay actividades en este viaje.")
                Button(onClick = onAddFirstActivity) {
                    Text("Agregar primera actividad")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ordered) { activity ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(activity.name)
                    Text(activity.startTime)
                    activity.description?.let { Text(it) }
                }
            }
        }
    }
}
