package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.usecase.trip.CreateTripUseCase
import com.voyager.tourism.domain.usecase.trip.GetTripsUseCase
import com.voyager.tourism.domain.usecase.trip.UpdateTripUseCase
import com.voyager.tourism.domain.usecase.trip.DeleteTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for trip-related operations
 * Handles trip creation, viewing, updating, and deletion
 */
@HiltViewModel
class TripViewModel @Inject constructor(
    private val createTripUseCase: CreateTripUseCase,
    private val getTripsUseCase: GetTripsUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {
    
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()
    
    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    val selectedTrip: StateFlow<Trip?> = _selectedTrip.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    /**
     * Loads trips for the user id stored in preferences, or surfaces an error when the session is missing.
     */
    fun loadTripsForCurrentUser() {
        val userId = preferencesManager.getCurrentUserId()
        if (userId.isNullOrBlank()) {
            _errorMessage.value = "Sin sesión: inicia sesión de nuevo"
            return
        }
        loadTrips(userId)
    }

    /**
     * Fetches all trips for [userId] and updates [trips] and [errorMessage] accordingly.
     */
    fun loadTrips(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            getTripsUseCase(userId)
                .onSuccess { tripList ->
                    _trips.value = tripList
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load trips"
                }
            _isLoading.value = false
        }
    }
    
    /**
     * Create a new trip
     */
    fun createTrip(trip: Trip) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            createTripUseCase(trip)
                .onSuccess { createdTrip ->
                    _trips.value = _trips.value + createdTrip
                    _successMessage.value = "Trip created successfully"
                    _selectedTrip.value = createdTrip
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to create trip"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Update an existing trip
     */
    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            updateTripUseCase(trip)
                .onSuccess { updatedTrip ->
                    val updatedList = _trips.value.map {
                        if (it.id == updatedTrip.id) updatedTrip else it
                    }
                    _trips.value = updatedList
                    _successMessage.value = "Trip updated successfully"
                    if (_selectedTrip.value?.id == updatedTrip.id) {
                        _selectedTrip.value = updatedTrip
                    }
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update trip"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Delete a trip
     */
    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            deleteTripUseCase(tripId)
                .onSuccess {
                    _trips.value = _trips.value.filter { it.id != tripId }
                    _successMessage.value = "Trip deleted successfully"
                    if (_selectedTrip.value?.id == tripId) {
                        _selectedTrip.value = null
                    }
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to delete trip"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Select a trip for detailed view
     */
    fun selectTrip(trip: Trip) {
        _selectedTrip.value = trip
    }
    
    /**
     * Clear selected trip
     */
    fun clearSelectedTrip() {
        _selectedTrip.value = null
    }
    
    /**
     * Clear messages
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
