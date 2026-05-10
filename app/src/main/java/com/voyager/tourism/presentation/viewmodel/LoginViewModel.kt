package com.voyager.tourism.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.voyager.tourism.domain.model.User
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
 * Handles login state and business logic including Native Google Sign-In
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {
    
    companion object {
        private const val UNKNOWN_ERROR = "Error desconocido"
    }

    private var googleSignInClient: GoogleSignInClient? = null
    
    // El Client ID del Backend (tipo Web Application) obtenido del .env del backend
    private val WEB_CLIENT_ID = "141800747513-e8sriq2r4dk7fq0909ga56f47i9e7llg.apps.googleusercontent.com"
    
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
                    val errorMessage = result.exceptionOrNull()?.message ?: UNKNOWN_ERROR
                    LoginUiState.Error(errorMessage)
                }
                else -> LoginUiState.Error(UNKNOWN_ERROR)
            }
        }
    }
    
    /**
     * Get Google Sign-In Intent to launch the account picker
     */
    fun getGoogleSignInIntent(context: Context): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestServerAuthCode(WEB_CLIENT_ID) // Esto genera el 'code' para el backend
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient!!.signInIntent
    }

    /**
     * Handle the result from Google Sign-In activity
     */
    fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val account = completedTask.getResult(ApiException::class.java)
                val code = account.serverAuthCode
                
                if (code == null) {
                    _uiState.value = LoginUiState.Error("No se pudo obtener el código del servidor")
                    return@launch
                }

                val result = loginUseCase.loginWithGoogle(code, null)
                
                _uiState.value = when {
                    result.isSuccess -> LoginUiState.Success(result.getOrThrow())
                    result.isFailure -> LoginUiState.Error(result.exceptionOrNull()?.message ?: "Error en el login")
                    else -> LoginUiState.Error(UNKNOWN_ERROR)
                }
            } catch (e: ApiException) {
                _uiState.value = LoginUiState.Error(apiExceptionToUserMessage(e))
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Error inesperado")
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

    private fun apiExceptionToUserMessage(e: ApiException): String = when (e.statusCode) {
        ConnectionResult.DEVELOPER_ERROR ->
            "Google no reconoce esta compilación de la app (código 10). En Google Cloud Console, " +
                "en el mismo proyecto que el cliente Web del backend, crea un ID de cliente OAuth " +
                "tipo Android: nombre de paquete com.voyager.tourism y SHA-1 del keystore con el que " +
                "firmas la APK (en debug: ./gradlew signingReport). Los cambios pueden tardar unos minutos."
        GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
            "Inicio de sesión con Google cancelado"
        else ->
            "Error de Google (${e.statusCode})${e.message?.takeIf { it.isNotBlank() }?.let { ": $it" } ?: ""}"
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
