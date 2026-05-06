package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.dto.TravelPlanActivityDto
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val travelPlanApi: TravelPlanApiService,
) : ViewModel() {

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> = _trip.asStateFlow()

    private val _activities = MutableStateFlow<List<TravelPlanActivityDto>>(emptyList())
    val activities: StateFlow<List<TravelPlanActivityDto>> = _activities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            tripRepository.getTripById(tripId)
                .onSuccess { _trip.value = it }
                .onFailure { _error.value = it.message ?: "No se pudo cargar el viaje" }

            val planId = tripId.toLongOrNull()
            if (planId != null) {
                runCatching {
                    val resp = travelPlanApi.getActivities(planId)
                    if (resp.status in 200..299) {
                        _activities.value = resp.data.orEmpty()
                    } else {
                        _error.value = resp.message.ifBlank { "Actividades: HTTP ${resp.status}" }
                    }
                }.onFailure {
                    _error.value = it.message ?: "Error al cargar actividades"
                }
            }
            _isLoading.value = false
        }
    }
}
