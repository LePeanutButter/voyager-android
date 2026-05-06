package com.voyager.tourism.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for login screen
 * Handles login state and business logic including Google OAuth2
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    /**
     * Login with username/email and password
     */
    fun login(usernameOrEmail: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            val result = loginUseCase(usernameOrEmail, password)
            
            _uiState.value = when {
                result.isSuccess -> {
                    val user = result.getOrThrow()
                    LoginUiState.Success(user)
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido"
                    LoginUiState.Error(errorMessage)
                }
                else -> LoginUiState.Error("Error desconocido")
            }
        }
    }
    
    /**
     * Initiate Google OAuth2 login flow
     * Opens browser with Google login URL
     */
    fun loginWithGoogle(context: Context) {
        try {
            // For now, we'll use a placeholder implementation
            // In a real implementation, this would call the backend to get the Google OAuth URL
            val googleLoginUrl = "https://accounts.google.com/oauth/authorize?" +
                "client_id=YOUR_GOOGLE_CLIENT_ID&" +
                "response_type=code&" +
                "scope=openid%20email%20profile&" +
                "redirect_uri=smartrip://auth/callback&" +
                "state=random_state_string"
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleLoginUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            
            _uiState.value = LoginUiState.GoogleLoginInitiated
        } catch (e: Exception) {
            _uiState.value = LoginUiState.Error("No se pudo iniciar el login con Google: ${e.message}")
        }
    }
    
    /**
     * Handle Google OAuth2 callback from deep link
     */
    fun handleGoogleCallback(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            try {
                val code = uri.getQueryParameter("code")
                val state = uri.getQueryParameter("state")
                
                if (code == null) {
                    _uiState.value = LoginUiState.Error("No se recibió código de autorización de Google")
                    return@launch
                }
                
                val result = loginUseCase.loginWithGoogle(code, state)
                
                _uiState.value = when {
                    result.isSuccess -> {
                        val user = result.getOrThrow()
                        LoginUiState.Success(user)
                    }
                    result.isFailure -> {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Error en el login con Google"
                        LoginUiState.Error(errorMessage)
                    }
                    else -> LoginUiState.Error("Error desconocido en el login con Google")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Error procesando el callback de Google: ${e.message}")
            }
        }
    }
    
    /**
     * Reset the UI state to idle
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

/**
 * Sealed class representing the login UI states
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object GoogleLoginInitiated : LoginUiState()
    data class Success(val user: com.voyager.tourism.domain.model.User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
