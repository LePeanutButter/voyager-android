package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.local.TokenManager
import com.voyager.tourism.domain.usecase.auth.GetUserUseCase
import com.voyager.tourism.domain.usecase.auth.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for profile screen
 * Handles profile state and business logic
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    /**
     * Load user profile data
     */
    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            val result = getUserUseCase(userId)
            
            _uiState.value = when {
                result.isSuccess -> {
                    val user = result.getOrThrow()
                    ProfileUiState.Loaded(user)
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error cargando perfil"
                    ProfileUiState.Error(errorMessage)
                }
                else -> ProfileUiState.Error("Error desconocido")
            }
        }
    }
    
    /**
     * Save user profile
     */
    fun saveProfile(
        userId: String,
        firstName: String,
        lastName: String,
        bio: String,
        phoneNumber: String?,
        interests: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Saving
            
            val result = updateProfileUseCase(
                userId = userId,
                firstName = firstName,
                lastName = lastName,
                bio = bio,
                phoneNumber = phoneNumber,
                interests = interests
            )
            
            _uiState.value = when {
                result.isSuccess -> {
                    val updatedUser = result.getOrThrow()
                    ProfileUiState.SaveSuccess(updatedUser)
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error guardando perfil"
                    ProfileUiState.Error(errorMessage)
                }
                else -> ProfileUiState.Error("Error desconocido")
            }
            
            // Return to loaded state after showing success
            if (result.isSuccess) {
                kotlinx.coroutines.delay(2000)
                _uiState.value = ProfileUiState.Loaded(result.getOrThrow())
            }
        }
    }
    
    /**
     * Reset the UI state
     */
    fun resetState() {
        _uiState.value = ProfileUiState.Loading
    }
    
    /**
     * Get current user ID from token manager
     */
    fun getCurrentUserId(): String? {
        return tokenManager.getCurrentUserId()
    }
}

/**
 * Sealed class representing the profile UI states
 */
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Loaded(val user: UserDto) : ProfileUiState()
    object Saving : ProfileUiState()
    data class SaveSuccess(val user: UserDto) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
