package com.voyager.tourism.presentation.ui.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.voyager.tourism.presentation.viewmodel.CreateTravelPlanViewModel
import com.voyager.tourism.presentation.viewmodel.CreateTravelPlanUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTravelPlanScreen(
    navController: NavController,
    viewModel: CreateTravelPlanViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var travelers by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    
    var titleError by remember { mutableStateOf<String?>(null) }
    var destinationError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }
    var travelersError by remember { mutableStateOf<String?>(null) }
    var budgetError by remember { mutableStateOf<String?>(null) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateTravelPlanUiState.Success -> {
                // Show success message and navigate back
                navController.navigate("dashboard") {
                    popUpTo("create_travel_plan") { inclusive = true }
                }
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Crear Plan de Viaje",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Title field
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
            enabled = uiState !is CreateTravelPlanUiState.Loading
        )
        
        // Destination field
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
            enabled = uiState !is CreateTravelPlanUiState.Loading
        )
        
        // Origin field
        OutlinedTextField(
            value = origin,
            onValueChange = { origin = it },
            label = { Text("Origen (opcional)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is CreateTravelPlanUiState.Loading
        )
        
        // Date fields row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Start Date field
            OutlinedTextField(
                value = startDate,
                onValueChange = { 
                    startDate = it
                    startDateError = if (it.isBlank()) "La fecha de inicio es requerida" else null
                    // Validate date range if end date is already set
                    if (endDate.isNotBlank()) {
                        endDateError = viewModel.validateDateRange(it, endDate)
                    }
                },
                label = { Text("Fecha de inicio *") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showStartDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                },
                isError = startDateError != null,
                supportingText = startDateError?.let { { Text(it) } },
                modifier = Modifier.weight(1f),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
                placeholder = { Text("yyyy-MM-dd") }
            )
            
            // End Date field
            OutlinedTextField(
                value = endDate,
                onValueChange = { 
                    endDate = it
                    endDateError = if (it.isBlank()) "La fecha de fin es requerida" else null
                    // Validate date range if start date is already set
                    if (startDate.isNotBlank()) {
                        endDateError = viewModel.validateDateRange(startDate, it)
                    }
                },
                label = { Text("Fecha de fin *") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                },
                isError = endDateError != null,
                supportingText = endDateError?.let { { Text(it) } },
                modifier = Modifier.weight(1f),
                enabled = uiState !is CreateTravelPlanUiState.Loading,
                placeholder = { Text("yyyy-MM-dd") }
            )
        }
        
        // Budget and Travelers row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Budget field
            OutlinedTextField(
                value = budget,
                onValueChange = { 
                    budget = it
                    budgetError = if (it.isNotBlank()) {
                        val budgetValue = it.toDoubleOrNull()
                        if (budgetValue == null || budgetValue < 0) {
                            "Presupuesto inválido"
                        } else null
                    } else null
                },
                label = { Text("Presupuesto estimado (opcional)") },
                leadingIcon = { Icon(Icons.Default.Money, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = budgetError != null,
                supportingText = budgetError?.let { { Text(it) } },
                modifier = Modifier.weight(1f),
                enabled = uiState !is CreateTravelPlanUiState.Loading
            )
            
            // Travelers field
            OutlinedTextField(
                value = travelers,
                onValueChange = { 
                    travelers = it
                    travelersError = if (it.isBlank()) {
                        "El número de viajeros es requerido"
                    } else {
                        val travelersValue = it.toIntOrNull()
                        if (travelersValue == null || travelersValue < 1) {
                            "Mínimo 1 viajero"
                        } else null
                    }
                },
                label = { Text("Número de viajeros *") },
                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = travelersError != null,
                supportingText = travelersError?.let { { Text(it) } },
                modifier = Modifier.weight(1f),
                enabled = uiState !is CreateTravelPlanUiState.Loading
            )
        }
        
        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            enabled = uiState !is CreateTravelPlanUiState.Loading
        )
        
        // Show error message if creation fails
        when (val state = uiState) {
            is CreateTravelPlanUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {}
        }
        
        // Create button
        Button(
            onClick = {
                // Validate all fields
                val hasError = validateTravelPlanFields(
                    title, destination, startDate, endDate, travelers, budget
                ) { field, error ->
                    when (field) {
                        "title" -> titleError = error
                        "destination" -> destinationError = error
                        "startDate" -> startDateError = error
                        "endDate" -> endDateError = error
                        "travelers" -> travelersError = error
                        "budget" -> budgetError = error
                    }
                }
                
                if (!hasError) {
                    viewModel.createPlan(
                        title = title,
                        destination = destination,
                        origin = origin,
                        startDate = startDate,
                        endDate = endDate,
                        budget = budget.toDoubleOrNull(),
                        travelers = travelers.toInt(),
                        description = description
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is CreateTravelPlanUiState.Loading
        ) {
            if (uiState is CreateTravelPlanUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Crear viaje")
            }
        }
    }
}

private fun validateTravelPlanFields(
    title: String,
    destination: String,
    startDate: String,
    endDate: String,
    travelers: String,
    budget: String,
    onError: (field: String, error: String) -> Unit
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
    
    if (travelers.isBlank()) {
        onError("travelers", "El número de viajeros es requerido")
        hasError = true
    } else {
        val travelersValue = travelers.toIntOrNull()
        if (travelersValue == null || travelersValue < 1) {
            onError("travelers", "Mínimo 1 viajero")
            hasError = true
        }
    }
    
    if (budget.isNotBlank()) {
        val budgetValue = budget.toDoubleOrNull()
        if (budgetValue == null || budgetValue < 0) {
            onError("budget", "Presupuesto inválido")
            hasError = true
        }
    }
    
    return hasError
}
