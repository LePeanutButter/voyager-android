package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.domain.usecase.social.GetCompatibleTravelersUseCase
import com.voyager.tourism.domain.usecase.social.SendConnectionRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for traveler matching functionality
 * 
 * Handles the state and business logic for finding compatible travelers
 * and sending connection requests.
 */
@HiltViewModel
class TravelerMatchingViewModel @Inject constructor(
    private val getCompatibleTravelersUseCase: GetCompatibleTravelersUseCase,
    private val sendConnectionRequestUseCase: SendConnectionRequestUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TravelerMatchingUiState())
    val uiState: StateFlow<TravelerMatchingUiState> = _uiState.asStateFlow()
    
    /**
     * Find compatible travelers for a specific travel plan
     */
    fun findCompatibleTravelers(travelPlanId: String, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getCompatibleTravelersUseCase(travelPlanId, token)
                .onSuccess { travelers ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        compatibleTravelers = travelers,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to find compatible travelers"
                    )
                }
        }
    }
    
    /**
     * Send a connection request to a compatible traveler
     */
    fun sendConnectionRequest(recipientId: Long, message: String?, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingRequest = true, error = null)
            
            sendConnectionRequestUseCase(recipientId, message, token)
                .onSuccess { connectionRequest ->
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        successMessage = "Connection request sent successfully!",
                        error = null
                    )
                    
                    // Refresh the list to update UI state
                    findCompatibleTravelers(_uiState.value.currentTravelPlanId, token)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSendingRequest = false,
                        error = exception.message ?: "Failed to send connection request"
                    )
                }
        }
    }
    
    /**
     * Set the current travel plan ID
     */
    fun setCurrentTravelPlanId(travelPlanId: String) {
        _uiState.value = _uiState.value.copy(currentTravelPlanId = travelPlanId)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

/**
 * UI state for traveler matching screen
 */
data class TravelerMatchingUiState(
    val isLoading: Boolean = false,
    val isSendingRequest: Boolean = false,
    val compatibleTravelers: List<TravelerMatchDto> = emptyList(),
    val currentTravelPlanId: String = "",
    val error: String? = null,
    val successMessage: String? = null
)
