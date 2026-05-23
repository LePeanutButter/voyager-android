package com.voyager.tourism.presentation.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.voyager.tourism.presentation.viewmodel.AuthViewModel
import com.voyager.tourism.presentation.viewmodel.CreateTravelPlanUiState
import com.voyager.tourism.presentation.viewmodel.CreateTravelPlanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formulario crear plan: validación alineada con el front (COP, viajeros, fechas, descripción, cronograma opcional).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("kotlin:S3776")
fun CreateTravelPlanScreen(
    navController: NavController,
    initialDestinationHint: String = "",
    viewModel: CreateTravelPlanViewModel = hiltViewModel(),
) {
    var title by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var travelers by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }

    val itineraryRows: SnapshotStateList<Pair<String, String>> = remember {
        mutableStateListOf("" to "")
    }

    var titleError by remember { mutableStateOf<String?>(null) }
    var destinationError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }
    var travelersError by remember { mutableStateOf<String?>(null) }
    var budgetError by remember { mutableStateOf<String?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(initialDestinationHint) {
        if (initialDestinationHint.isNotBlank()) {
            destination = initialDestinationHint
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateTravelPlanUiState.Success -> {
                navController.navigate(AuthViewModel.ROUTE_DASHBOARD) {
                    popUpTo(AuthViewModel.ROUTE_DASHBOARD) { inclusive = false }
                    launchSingleTop = true
                }
            }
            else -> {}
        }
    }

    val startPickerState = rememberDatePickerState()
    val endPickerState = rememberDatePickerState()

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startPickerState.selectedDateMillis?.let { ms ->
                            startDate = formatYmdUtc(ms)
                            startDateError = null
                            if (endDate.isNotBlank()) {
                                endDateError = viewModel.validateDateRange(startDate, endDate)
                                if (endDateError != null && compareYmd(startDate, endDate) > 0) {
                                    endDate = startDate
                                    endDateError = null
                                }
                            }
                        }
                        showStartDatePicker = false
                    },
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = startPickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endPickerState.selectedDateMillis?.let { ms ->
                            val picked = formatYmdUtc(ms)
                            if (startDate.isNotBlank() && compareYmd(startDate, picked) > 0) {
                                endDateError = "La fecha de fin no puede ser anterior al inicio."
                            } else {
                                endDate = picked
                                endDateError = null
                            }
                        }
                        showEndDatePicker = false
                    },
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = endPickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear plan de viaje") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Planea tu próxima aventura. Los importes son en COP, como en el cliente web.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = if (it.isBlank()) "El título es requerido" else null
                },
                label = { Text("Título *") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
            )

            OutlinedTextField(
                value = destination,
                onValueChange = {
                    destination = it
                    destinationError = if (it.isBlank()) "El destino es requerido" else null
                },
                label = { Text("Destino *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                isError = destinationError != null,
                supportingText = destinationError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
            )

            OutlinedTextField(
                value = origin,
                onValueChange = { origin = it },
                label = { Text("Origen (opcional)") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
            )

            Text("Fechas *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    if (startDate.isBlank()) "Fecha de inicio — tocar para elegir" else "Inicio: $startDate",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
            }
            startDateError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    if (endDate.isBlank()) "Fecha de fin — tocar para elegir" else "Fin: $endDate",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
            }
            endDateError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            OutlinedTextField(
                value = budget,
                onValueChange = { raw ->
                    budget = digitsOnly(raw)
                    budgetError = if (budget.isNotBlank()) validateCopBudgetString(budget) else null
                },
                label = { Text("Presupuesto estimado (COP) *") },
                leadingIcon = { Icon(Icons.Default.Money, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = budgetError != null,
                supportingText = {
                    val preview = budget.toLongOrNull()?.let { formatCop(roundCop(it)) }
                    Column {
                        budgetError?.let { Text(it) }
                        Text(
                            "Mínimo ${formatCop(COP_MIN)}, múltiplos de $COP_STEP. Sin puntos ni comas." +
                                (preview?.let { p -> " Valor ajustado: $p" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
            )

            OutlinedTextField(
                value = travelers,
                onValueChange = { raw ->
                    val d = digitsOnly(raw).dropWhile { it == '0' }
                    travelers = if (d.isEmpty()) "1" else d
                    travelersError = when {
                        travelers.isBlank() -> "Indica cuántos viajeros (mínimo 1)."
                        (travelers.toIntOrNull() ?: 0) < 1 -> "Mínimo 1 viajero."
                        else -> null
                    }
                },
                label = { Text("Número de viajeros *") },
                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = travelersError != null,
                supportingText = {
                    Column {
                        travelersError?.let { Text(it) }
                        Text("Solo números (mínimo 1).", style = MaterialTheme.typography.bodySmall)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
            )

            OutlinedTextField(
                value = description,
                onValueChange = { v ->
                    description = v.take(DESCRIPTION_MAX_LEN)
                },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                enabled = uiState !is CreateTravelPlanUiState.Loading,
                supportingText = {
                    Text("${description.length} / $DESCRIPTION_MAX_LEN caracteres")
                },
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Cronograma (opcional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Añade hitos por día; se guardarán al final de la descripción.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            itineraryRows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    OutlinedTextField(
                        value = row.first,
                        onValueChange = { v ->
                            itineraryRows[index] = v to row.second
                        },
                        label = { Text("Día / título") },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = row.second,
                        onValueChange = { v ->
                            itineraryRows[index] = row.first to v
                        },
                        label = { Text("Actividad") },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            if (itineraryRows.size > 1) itineraryRows.removeAt(index)
                        },
                        enabled = itineraryRows.size > 1,
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Quitar fila")
                    }
                }
            }
            FilledTonalButton(
                onClick = { itineraryRows.add("" to "") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Añadir día al cronograma")
            }

            when (val state = uiState) {
                is CreateTravelPlanUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                else -> {}
            }

            Button(
                onClick = {
                    val cronograma = buildItineraryBlock(itineraryRows)
                    val mergedDescription = (description.trim() + cronograma).take(DESCRIPTION_MAX_LEN)
                    val hasError = validateTravelPlanFields(
                        title = title,
                        destination = destination,
                        startDate = startDate,
                        endDate = endDate,
                        travelers = travelers,
                        budget = budget,
                        onError = { field, err ->
                            when (field) {
                                "title" -> titleError = err
                                "destination" -> destinationError = err
                                "startDate" -> startDateError = err
                                "endDate" -> endDateError = err
                                "travelers" -> travelersError = err
                                "budget" -> budgetError = err
                            }
                        },
                    )
                    if (!hasError) {
                        val tv = travelers.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        val rawBudget = budget.toLongOrNull() ?: 0L
                        val rounded = roundCop(rawBudget).toDouble()
                        viewModel.createPlan(
                            title = title,
                            destination = destination,
                            origin = origin,
                            startDate = startDate,
                            endDate = endDate,
                            budget = rounded,
                            travelers = tv,
                            description = mergedDescription,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
            ) {
                if (uiState is CreateTravelPlanUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Crear viaje")
                }
            }
        }
    }
}

private fun formatYmdUtc(millis: Long): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    fmt.timeZone = TimeZone.getTimeZone("UTC")
    return fmt.format(Date(millis))
}

/** Compara yyyy-MM-dd; >0 si a > b */
private fun compareYmd(a: String, b: String): Int {
    val da = a.trim().take(10)
    val db = b.trim().take(10)
    return da.compareTo(db)
}

private fun buildItineraryBlock(rows: List<Pair<String, String>>): String {
    val lines = rows.mapNotNull { (day, act) ->
        val d = day.trim()
        val a = act.trim()
        if (d.isEmpty() && a.isEmpty()) return@mapNotNull null
        when {
            d.isNotEmpty() && a.isNotEmpty() -> "$d: $a"
            d.isNotEmpty() -> d
            else -> a
        }
    }
    if (lines.isEmpty()) return ""
    return "\n\n--- Cronograma ---\n" + lines.joinToString("\n")
}

private fun validateTravelPlanFields(
    title: String,
    destination: String,
    startDate: String,
    endDate: String,
    travelers: String,
    budget: String,
    onError: (field: String, error: String) -> Unit,
): Boolean {
    var hasError = false
    if (title.isBlank()) {
        onError("title", "El título es requerido")
        hasError = true
    }
    if (destination.isBlank()) {
        onError("destination", "El destino es requerido")
        hasError = true
    }
    if (startDate.isBlank()) {
        onError("startDate", "La fecha de inicio es requerida")
        hasError = true
    }
    if (endDate.isBlank()) {
        onError("endDate", "La fecha de fin es requerida")
        hasError = true
    }
    if (startDate.isNotBlank() && endDate.isNotBlank() && compareYmd(endDate, startDate) < 0) {
        onError("endDate", "La fecha de fin no puede ser anterior al inicio.")
        hasError = true
    }
    val tv = travelers.toIntOrNull() ?: 0
    if (tv < 1) {
        onError("travelers", "Mínimo 1 viajero.")
        hasError = true
    }
    validateCopBudgetString(budget)?.let {
        onError("budget", it)
        hasError = true
    }
    return hasError
}
