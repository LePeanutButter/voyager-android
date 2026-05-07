package com.voyager.tourism.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.domain.repository.AuthRepository
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
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository,
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
        viewModelScope.launch {
            try {
                val urlResult = authRepository.initiateGoogleLogin()
                if (urlResult.isFailure) {
                    _uiState.value = LoginUiState.Error(
                        urlResult.exceptionOrNull()?.message ?: "No se pudo obtener la URL de Google",
                    )
                    return@launch
                }
                val url = urlResult.getOrThrow()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                _uiState.value = LoginUiState.GoogleLoginInitiated
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("No se pudo iniciar el login con Google: ${e.message}")
            }
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
