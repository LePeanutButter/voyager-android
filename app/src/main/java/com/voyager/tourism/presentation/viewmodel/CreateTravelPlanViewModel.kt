package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.domain.usecase.trip.CreateTravelPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for creating travel plans
 * Handles travel plan creation state and business logic
 */
@HiltViewModel
class CreateTravelPlanViewModel @Inject constructor(
    private val createTravelPlanUseCase: CreateTravelPlanUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CreateTravelPlanUiState>(CreateTravelPlanUiState.Idle)
    val uiState: StateFlow<CreateTravelPlanUiState> = _uiState.asStateFlow()
    
    /**
     * Create a new travel plan
     */
    @Suppress("kotlin:S107")
    fun createPlan(
        title: String,
        destination: String,
        origin: String,
        startDate: String,
        endDate: String,
        budget: Double?,
        travelers: Int,
        description: String
    ) {
        viewModelScope.launch {
            _uiState.value = CreateTravelPlanUiState.Loading
            
            val result = createTravelPlanUseCase(
                title = title,
                destination = destination,
                origin = origin.ifBlank { null },
                startDate = startDate,
                endDate = endDate,
                budget = budget,
                travelers = travelers,
                description = description.ifBlank { null }
            )
            
            _uiState.value = when {
                result.isSuccess -> {
                    val travelPlan = result.getOrThrow()
                    CreateTravelPlanUiState.Success(travelPlan)
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                    CreateTravelPlanUiState.Error(errorMessage)
                }
                else -> CreateTravelPlanUiState.Error("Error desconocido")
            }
        }
    }
    
    /**
     * Reset the UI state to idle
     */
    fun resetState() {
        _uiState.value = CreateTravelPlanUiState.Idle
    }
    
    /**
     * Validate date range locally before calling API
     */
    fun validateDateRange(startDate: String, endDate: String): String? {
        if (startDate.isBlank() || endDate.isBlank()) {
            return null // Let the use case handle empty validation
        }
        
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)
            
            if (start != null && end != null && end.before(start)) {
                "El rango de fechas es inválido"
            } else null
        } catch (e: Exception) {
            null // Let the use case handle format validation
        }
    }
}

/**
 * Sealed class representing the create travel plan UI states
 */
sealed class CreateTravelPlanUiState {
    object Idle : CreateTravelPlanUiState()
    object Loading : CreateTravelPlanUiState()
    data class Success(val travelPlan: TravelPlanDto) : CreateTravelPlanUiState()
    data class Error(val message: String) : CreateTravelPlanUiState()
}
