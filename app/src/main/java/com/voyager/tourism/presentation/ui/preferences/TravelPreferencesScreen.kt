package com.voyager.tourism.presentation.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.domain.model.QuestionnaireAnswer
import com.voyager.tourism.presentation.viewmodel.TravelPreferencesViewModel

@Composable
fun TravelPreferencesScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: TravelPreferencesViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val awaitingFinal by viewModel.awaitingFinalSubmit.collectAsState()
    val submitResult by viewModel.submitResult.collectAsState()
    val derived by viewModel.derivedCategory.collectAsState()

    val selections = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(userId) {
        viewModel.startOrRefresh(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Preferencias de viaje",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Cuestionario adaptativo para el motor de IA",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }

        error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { viewModel.clearError(); viewModel.startOrRefresh(userId) }) {
                Text("Reintentar")
            }
        }

        derived?.let { d ->
            Text(
                text = "Categoría actual: $d",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
        }

        if (submitResult != null) {
            val r = submitResult!!
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Perfil guardado", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Categoría principal: ${r.primaryCategory}")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        r.aiContextSummary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Volver") }
            return
        }

        if (awaitingFinal && !isLoading) {
            Text(
                text = "Has completado el cuestionario. Guarda para alimentar el motor de IA.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { viewModel.finalize(userId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Guardar preferencias")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack) { Text("Cancelar") }
            return
        }

        questions.forEach { q ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .selectableGroup()
                ) {
                    Text(q.prompt, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    q.options.forEach { opt ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selections[q.id] == opt.id,
                                    onClick = { selections[q.id] = opt.id },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selections[q.id] == opt.id,
                                onClick = null
                            )
                            Text(
                                opt.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (questions.isNotEmpty() && !awaitingFinal) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val missing = questions.any { selections[it.id] == null }
                    if (missing) {
                        return@Button
                    }
                    val answers = questions.mapNotNull { qq ->
                        selections[qq.id]?.let { oid ->
                            QuestionnaireAnswer(qq.id, listOf(oid))
                        }
                    }
                    viewModel.sendStep(userId, answers)
                    selections.clear()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && questions.all { selections[it.id] != null }
            ) {
                Text("Siguiente")
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack) { Text("Volver") }
    }
}
