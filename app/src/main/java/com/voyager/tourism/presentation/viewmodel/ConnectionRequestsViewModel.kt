package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.domain.usecase.social.GetPendingRequestsUseCase
import com.voyager.tourism.domain.usecase.social.RespondToConnectionRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for connection requests functionality
 * 
 * Handles the state and business logic for managing connection requests
 * including viewing pending requests and accepting/rejecting them.
 */
@HiltViewModel
class ConnectionRequestsViewModel @Inject constructor(
    private val getPendingRequestsUseCase: GetPendingRequestsUseCase,
    private val respondToConnectionRequestUseCase: RespondToConnectionRequestUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConnectionRequestsUiState())
    val uiState: StateFlow<ConnectionRequestsUiState> = _uiState.asStateFlow()
    
    /**
     * Load pending connection requests
     */
    fun loadPendingRequests(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getPendingRequestsUseCase(token)
                .onSuccess { requests ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pendingRequests = requests,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load pending requests"
                    )
                }
        }
    }
    
    /**
     * Accept a connection request
     */
    fun acceptRequest(requestId: String, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            
            respondToConnectionRequestUseCase.acceptRequest(requestId, token)
                .onSuccess { _ ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "Connection request accepted!",
                        error = null
                    )
                    
                    // Refresh the list
                    loadPendingRequests(token)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = exception.message ?: "Failed to accept connection request"
                    )
                }
        }
    }
    
    /**
     * Reject a connection request
     */
    fun rejectRequest(requestId: String, token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            
            respondToConnectionRequestUseCase.rejectRequest(requestId, token)
                .onSuccess { _ ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "Connection request rejected",
                        error = null
                    )
                    
                    // Refresh the list
                    loadPendingRequests(token)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = exception.message ?: "Failed to reject connection request"
                    )
                }
        }
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
 * UI state for connection requests screen
 */
data class ConnectionRequestsUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val pendingRequests: List<ConnectionRequestDto> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
