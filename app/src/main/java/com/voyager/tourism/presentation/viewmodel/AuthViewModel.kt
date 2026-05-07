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

/**
 * Application-wide authentication coordinator: credentials login, registration, Google OAuth deep links, and session expiry.
 */
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

    /**
     * Signs in with email (or username) and password, updating [currentUser] and [authState] on success.
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
     * Creates a new account and mirrors a successful [login] outcome on the same flows.
     */
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

    /**
     * Clears local session via [UserRepository] and sets state to unauthenticated.
     */
    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * Called when tokens are invalid or revoked; clears the user and navigates to the login route.
     */
    fun onSessionExpired() {
        viewModelScope.launch {
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            _navigateAfterAuth.tryEmit(ROUTE_LOGIN)
        }
    }

    /**
     * Completes Google OAuth2 using query parameters on a `smartrip://auth` deep link (`code`, optional `state`).
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

    /** Clears [oauthError] after the user dismisses an OAuth failure message. */
    fun clearOAuthError() {
        _oauthError.value = null
    }

    /**
     * Restores [authState] and [currentUser] from [UserRepository] on cold start.
     */
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

    /** Clears credential login or registration error messages. */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Adopts a user already persisted by another flow (for example a dedicated login screen) into this ViewModel state.
     */
    fun adoptAuthenticatedUser(user: User) {
        _currentUser.value = user
        _authState.value = AuthState.Authenticated
    }

    companion object {
        const val ROUTE_LOGIN = "login"
        const val ROUTE_DASHBOARD = "dashboard"
    }
}

/**
 * Root-level auth status exposed to navigation: loading bootstrap, signed-in user, or explicit sign-out.
 */
sealed class AuthState {
    object Loading : AuthState()
    object Idle : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
