package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for user registration screen
 * Handles registration state and business logic
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    /**
     * Register a new user with the provided credentials
     */
    fun register(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            
            val result = registerUseCase(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName
            )
            
            _uiState.value = when {
                result.isSuccess -> {
                    RegisterUiState.Success
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                    RegisterUiState.Error(errorMessage)
                }
                else -> RegisterUiState.Error("Error desconocido")
            }
        }
    }
    
    /**
     * Reset the UI state to idle
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

/**
 * Sealed class representing the registration UI states
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
