package com.voyager.tourism.presentation.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.presentation.ui.trip.spanishLabel
import com.voyager.tourism.presentation.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val weekLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

private fun dateKey(cal: Calendar): String {
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    val d = cal.get(Calendar.DAY_OF_MONTH)
    return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
}

private fun startOfDayUtc(millis: Long): Calendar =
    Calendar.getInstance(TimeZone.getDefault()).apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

private fun buildMonthDays(cursorMonth: Calendar): List<Calendar> {
    val monthStart = cursorMonth.clone() as Calendar
    monthStart.set(Calendar.DAY_OF_MONTH, 1)
    monthStart.set(Calendar.HOUR_OF_DAY, 0)
    monthStart.set(Calendar.MINUTE, 0)
    monthStart.set(Calendar.SECOND, 0)
    monthStart.set(Calendar.MILLISECOND, 0)
    val firstWeekday = monthStart.get(Calendar.DAY_OF_WEEK)
    val mondayOffset = if (firstWeekday == Calendar.SUNDAY) 6 else firstWeekday - Calendar.MONDAY
    val gridStart = monthStart.clone() as Calendar
    gridStart.add(Calendar.DAY_OF_MONTH, -mondayOffset)
    return List(42) { i ->
        (gridStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) }
    }
}

private fun plansByDateKey(trips: List<Trip>): Map<String, List<Trip>> {
    val map = mutableMapOf<String, MutableList<Trip>>()
    trips.forEach { trip ->
        val start = startOfDayUtc(trip.startDate)
        val endRaw = startOfDayUtc(trip.endDate)
        val end = if (endRaw.timeInMillis < start.timeInMillis) start.clone() as Calendar else endRaw
        val cursor = start.clone() as Calendar
        var guard = 0
        while (cursor.timeInMillis <= end.timeInMillis && guard < 400) {
            val key = dateKey(cursor)
            map.getOrPut(key) { mutableListOf() }.add(trip)
            cursor.add(Calendar.DAY_OF_MONTH, 1)
            guard++
        }
    }
    return map
}

/**
 * Vista mensual de planes (equivalente a [voyager-web-client/src/pages/Calendar/CalendarPage.jsx]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCalendarScreen(
    userId: String,
    onBack: () -> Unit,
    onTripClick: (String) -> Unit,
    onCreatePlan: () -> Unit,
    onViewAllTrips: () -> Unit,
    tripViewModel: TripViewModel = hiltViewModel(),
) {
    val trips by tripViewModel.trips.collectAsState()
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) tripViewModel.loadTrips(userId)
    }

    var cursorMonth by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            },
        )
    }

    val monthDays = remember(cursorMonth) { buildMonthDays(cursorMonth) }
    val byDay = remember(trips) { plansByDateKey(trips) }
    val todayKey = remember { dateKey(Calendar.getInstance()) }
    val visibleMonth = cursorMonth.get(Calendar.MONTH)

    val monthTitle = remember(cursorMonth) {
        SimpleDateFormat("LLLL yyyy", Locale("es", "CO")).format(cursorMonth.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    val upcoming = remember(trips) {
        val now = startOfDayUtc(System.currentTimeMillis())
        trips
            .filter { trip ->
                val end = startOfDayUtc(trip.endDate)
                end.timeInMillis >= now.timeInMillis
            }
            .sortedBy { it.startDate }
            .take(8)
    }

    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale("es", "CO")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Visualiza tus planes en formato mensual, al estilo agenda.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        IconButton(
                            onClick = {
                                val c = cursorMonth.clone() as Calendar
                                c.add(Calendar.MONTH, -1)
                                cursorMonth = c
                            },
                        ) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
                        }
                        Text(monthTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        IconButton(
                            onClick = {
                                val c = cursorMonth.clone() as Calendar
                                c.add(Calendar.MONTH, 1)
                                cursorMonth = c
                            },
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Mes siguiente")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        weekLabels.forEach { label ->
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    monthDays.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            week.forEach { day ->
                                val key = dateKey(day)
                                val dayPlans = byDay[key].orEmpty()
                                val outside = day.get(Calendar.MONTH) != visibleMonth
                                val isToday = key == todayKey
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.72f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                                outside -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            },
                                        )
                                        .padding(4.dp),
                                ) {
                                    Text(
                                        "${day.get(Calendar.DAY_OF_MONTH)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (outside) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                    )
                                    dayPlans.take(2).forEach { plan ->
                                        Text(
                                            plan.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .clickable { onTripClick(plan.id) }
                                                .padding(horizontal = 2.dp, vertical = 1.dp),
                                        )
                                    }
                                    if (dayPlans.size > 2) {
                                        Text(
                                            "+${dayPlans.size - 2} más",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Próximos planes", style = MaterialTheme.typography.titleMedium)
                        Text("${upcoming.size}", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (upcoming.isEmpty()) {
                        Text(
                            "Aún no tienes planes futuros para mostrar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        upcoming.forEach { plan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTripClick(plan.id) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(plan.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(
                                        dateFmt.format(Date(plan.startDate)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            plan.destination.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                Text(plan.status.spanishLabel(), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(onClick = onCreatePlan, modifier = Modifier.weight(1f)) {
                    Text("Planear nuevo viaje")
                }
                TextButton(onClick = onViewAllTrips, modifier = Modifier.weight(1f)) {
                    Text("Ver todos mis viajes")
                }
            }
        }
    }
}
