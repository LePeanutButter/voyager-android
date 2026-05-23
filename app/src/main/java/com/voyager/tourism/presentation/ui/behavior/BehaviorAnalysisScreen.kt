package com.voyager.tourism.presentation.ui.behavior

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.data.dto.BehaviorSummary
import com.voyager.tourism.presentation.viewmodel.BehaviorAnalysisViewModel
import com.voyager.tourism.presentation.viewmodel.BehaviorAnalysisUiState

/**
 * Equivalente a [BehaviorAnalysisPage.jsx] + resumen tipo [BehaviorAnalysisDashboard.jsx].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorAnalysisScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: BehaviorAnalysisViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val summary by viewModel.behaviorSummary.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.getBehaviorSummary(userId, days = 30)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Behavior Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
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
        ) {
            Text(
                text = "Understand your travel preferences through intelligent behavior tracking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))

            BehaviorAnalysisStatusCards(state = ui)

            Button(
                onClick = { viewModel.analyzeUserBehavior(userId, analysisPeriodDays = 7) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.isLoading && userId.isNotBlank(),
            ) {
                Text("Run analysis")
            }

            Spacer(modifier = Modifier.height(16.dp))

            summary?.let { s ->
                BehaviorSummaryCard(summary = s)
            }
        }
    }
}

@Composable
private fun BoxAlignedProgress() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BehaviorAnalysisStatusCards(state: BehaviorAnalysisUiState) {
    if (state.isLoading) {
        BoxAlignedProgress()
    }

    state.error?.let { err ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        ) {
            Text(
                text = err,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    state.successMessage?.let { msg ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(
                text = msg,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun BehaviorSummaryCard(summary: BehaviorSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Behavior Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SummaryStat("${summary.totalInteractions}", "Total interactions")
                SummaryStat("${summary.analysisPeriodDays}", "Period (days)")
                SummaryStat("${summary.recentPatterns.size}", "Patterns")
            }
            if (summary.interactionBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Interaction breakdown", fontWeight = FontWeight.SemiBold)
                summary.interactionBreakdown.entries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(entry.key, style = MaterialTheme.typography.bodyMedium)
                        Text("${entry.value}", fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (summary.categoryBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Category preferences", fontWeight = FontWeight.SemiBold)
                summary.categoryBreakdown.entries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(entry.key, style = MaterialTheme.typography.bodyMedium)
                        Text("${entry.value}", fontWeight = FontWeight.Medium)
                    }
                }
            }
            summary.lastAnalysis?.let { dt ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Last analysis: $dt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
