package com.voyager.tourism.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.session.SessionInvalidationNotifier
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.usecase.auth.LoginUseCase
import com.voyager.tourism.domain.usecase.auth.RegisterUseCase
import com.voyager.tourism.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val userRepository: UserRepository,
    private val sessionInvalidationNotifier: SessionInvalidationNotifier,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _oauthError = MutableStateFlow<String?>(null)
    val oauthError: StateFlow<String?> = _oauthError.asStateFlow()

    private val _navigateAfterAuth = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateAfterAuth: SharedFlow<String> = _navigateAfterAuth.asSharedFlow()

    init {
        checkAuthenticationStatus()
        viewModelScope.launch {
            sessionInvalidationNotifier.events.collect {
                onSessionExpired()
            }
        }
    }

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

    fun register(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
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

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun onSessionExpired() {
        viewModelScope.launch {
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            _navigateAfterAuth.tryEmit(ROUTE_LOGIN)
        }
    }

    /**
     * Intercambio de código OAuth2 (deep link smartrip://auth?code=...).
     */
    fun handleGoogleOAuthUri(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            _oauthError.value = null
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            if (error != null) {
                _oauthError.value = uri.getQueryParameter("error_description") ?: error
                return@launch
            }
            if (code.isNullOrBlank()) {
                _oauthError.value = "No se recibió el código de autorización"
                return@launch
            }
            _isLoading.value = true
            val state = uri.getQueryParameter("state")
            loginUseCase.loginWithGoogle(code, state)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                    _navigateAfterAuth.tryEmit(ROUTE_DASHBOARD)
                }
                .onFailure { e ->
                    _oauthError.value = e.message ?: "Error en login con Google"
                    _authState.value = AuthState.Unauthenticated
                }
            _isLoading.value = false
        }
    }

    fun clearOAuthError() {
        _oauthError.value = null
    }

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            userRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated
                    } else {
                        _currentUser.value = null
                        _authState.value = AuthState.Unauthenticated
                    }
                }
                .onFailure {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /** Tras login/registro desde pantallas que usan otro ViewModel pero ya persisten sesión. */
    fun adoptAuthenticatedUser(user: User) {
        _currentUser.value = user
        _authState.value = AuthState.Authenticated
    }

    companion object {
        const val ROUTE_LOGIN = "login"
        const val ROUTE_DASHBOARD = "dashboard"
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Idle : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
