package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.usecase.auth.LoginUseCase
import com.voyager.tourism.domain.usecase.auth.RegisterUseCase
import com.voyager.tourism.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication-related operations
 * Handles login, registration, and authentication state management
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        checkAuthenticationStatus()
    }
    
    /**
     * Login user with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            loginUseCase(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Login failed"
                    _authState.value = AuthState.Error(exception.message ?: "Login failed")
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Register new user
     */
    fun register(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            registerUseCase(email, password, username, firstName, lastName)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Registration failed"
                    _authState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
                .onSuccess {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
                .onFailure { exception ->
                    // Still clear local state even if API call fails
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
        }
    }
    
    /**
     * Check if user is already authenticated
     */
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
                .onFailure {
                    _authState.value = AuthState.Unauthenticated
                }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * Sealed class representing authentication states
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
