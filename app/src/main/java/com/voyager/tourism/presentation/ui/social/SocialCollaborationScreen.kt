package com.voyager.tourism.presentation.ui.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.voyager.tourism.domain.model.SharedActivity
import com.voyager.tourism.domain.model.SharedActivityDecision
import com.voyager.tourism.presentation.viewmodel.SocialCollaborationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialCollaborationScreen(
    onBack: (() -> Unit)? = null,
    viewModel: SocialCollaborationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    var userIdInput by remember { mutableStateOf("") }
    var travelPlanIdInput by remember { mutableStateOf("") }
    var selectedActivityId by remember { mutableStateOf("") }
    var selectedReceiverId by remember { mutableStateOf("") }
    var pendingSharedActivityId by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var interestsInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.clearFeedback()
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Colaboración social") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    },
                )
            }
        },
    ) { padding ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Shared Activities", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Share activity plans and review incoming decisions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = userIdInput,
                        onValueChange = { userIdInput = it },
                        label = { Text("Your user ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { userIdInput.toLongOrNull()?.let(viewModel::loadConnections) },
                            enabled = !state.isLoadingConnections
                        ) { Text("Load Travelers") }
                        if (state.isLoadingConnections) CircularProgressIndicator()
                    }
                    state.connections.forEach { connection ->
                        AssistChip(
                            onClick = { selectedReceiverId = connection.id.toString() },
                            label = { Text("${connection.username} (${connection.status})") }
                        )
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = travelPlanIdInput,
                        onValueChange = { travelPlanIdInput = it },
                        label = { Text("Travel plan ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { travelPlanIdInput.toLongOrNull()?.let(viewModel::loadActivities) },
                            enabled = !state.isLoadingActivities
                        ) { Text("Load Activities") }
                        if (state.isLoadingActivities) CircularProgressIndicator()
                    }
                    state.activities.forEach { activity ->
                        AssistChip(
                            onClick = { selectedActivityId = activity.id.toString() },
                            label = { Text(activity.name.ifBlank { "Activity ${activity.id}" }) }
                        )
                    }
                    OutlinedTextField(
                        value = selectedActivityId,
                        onValueChange = { selectedActivityId = it },
                        label = { Text("Selected activity ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = selectedReceiverId,
                        onValueChange = { selectedReceiverId = it },
                        label = { Text("Selected receiver ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val activityId = selectedActivityId.toLongOrNull()
                            val receiverId = selectedReceiverId.toLongOrNull()
                            if (activityId != null && receiverId != null) {
                                viewModel.shareActivity(activityId, receiverId)
                            }
                        },
                        enabled = !state.isSubmittingShare
                    ) {
                        Text(if (state.isSubmittingShare) "Sharing..." else "Share Activity")
                    }
                }
            }
        }

        item {
            Text("Shared Requests", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pendingSharedActivityId,
                        onValueChange = { pendingSharedActivityId = it },
                        label = { Text("Incoming shared activity ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                pendingSharedActivityId.toLongOrNull()?.let {
                                    viewModel.resolveSharedActivity(it, SharedActivityDecision.ACCEPT)
                                }
                            },
                            enabled = !state.isResolvingSharedActivity
                        ) { Text("Accept by ID") }
                        Button(
                            onClick = {
                                pendingSharedActivityId.toLongOrNull()?.let {
                                    viewModel.resolveSharedActivity(it, SharedActivityDecision.REJECT)
                                }
                            },
                            enabled = !state.isResolvingSharedActivity
                        ) { Text("Reject by ID") }
                    }
                }
            }
        }

        if (state.sharedActivities.isEmpty()) {
            item {
                EmptyStateCard("No shared activities yet.")
            }
        } else {
            items(state.sharedActivities) { shared ->
                SharedActivityCard(
                    sharedActivity = shared,
                    isResolving = state.isResolvingSharedActivity,
                    onAccept = {
                        viewModel.trackSharedActivity(shared.copy(status = "PENDING"))
                        viewModel.resolveSharedActivity(shared.id, SharedActivityDecision.ACCEPT)
                    },
                    onReject = {
                        viewModel.trackSharedActivity(shared.copy(status = "PENDING"))
                        viewModel.resolveSharedActivity(shared.id, SharedActivityDecision.REJECT)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Compatibility Matching", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(destination, { destination = it }, label = { Text("Destination") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(startDate, { startDate = it }, label = { Text("Start date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(endDate, { endDate = it }, label = { Text("End date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        interestsInput,
                        { interestsInput = it },
                        label = { Text("Interests (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val interests = interestsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            viewModel.loadCompatibilityMatches(destination, startDate, endDate, interests)
                        },
                        enabled = !state.isLoadingMatches
                    ) {
                        Text(if (state.isLoadingMatches) "Loading..." else "Find Matches")
                    }
                }
            }
        }

        item {
            val availableInterests = state.allMatches.flatMap { it.matchedInterests }.distinct()
            if (availableInterests.isNotEmpty()) {
                Text("Filter by Interests", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableInterests.take(6).forEach { interest ->
                        val selected = interest in state.selectedInterests
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val next = state.selectedInterests.toMutableSet().apply {
                                    if (selected) remove(interest) else add(interest)
                                }
                                viewModel.applyInterestsFilter(next)
                            },
                            label = { Text(interest) }
                        )
                    }
                }
            }
        }

        if (state.isLoadingMatches) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (state.filteredMatches.isEmpty()) {
            item {
                EmptyStateCard("No compatibility matches found for this criteria.")
            }
        } else {
            items(state.filteredMatches) { match ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Traveler #${match.userId}", fontWeight = FontWeight.SemiBold)
                        Text("Total Score: ${"%.2f".format(match.totalScore)}")
                        Text("Destination: ${"%.2f".format(match.destinationScore)} | Date: ${"%.2f".format(match.dateProximityScore)} | Interests: ${"%.2f".format(match.interestScore)}")
                        if (match.matchedInterests.isNotEmpty()) {
                            Text("Matched interests: ${match.matchedInterests.joinToString()}")
                        }
                    }
                }
            }
        }

        state.successMessage?.let { msg ->
            item { StatusCard(msg, false) }
        }
        state.errorMessage?.let { msg ->
            item { StatusCard(msg, true) }
        }
    }
    }
}

@Composable
private fun SharedActivityCard(
    sharedActivity: SharedActivity,
    isResolving: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Activity #${sharedActivity.activityId} - Status: ${sharedActivity.status}")
            if (sharedActivity.status == "PENDING") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAccept, enabled = !isResolving) { Text("Accept") }
                    Button(onClick = onReject, enabled = !isResolving) { Text("Reject") }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusCard(message: String, isError: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}
