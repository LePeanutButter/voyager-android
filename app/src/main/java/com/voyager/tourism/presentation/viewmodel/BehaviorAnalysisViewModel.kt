package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.data.dto.*
import com.voyager.tourism.domain.usecase.behavior.AnalyzeUserBehaviorUseCase
import com.voyager.tourism.domain.usecase.behavior.TrackInteractionUseCase
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Behavior Analysis feature
 * Manages UI state for behavior tracking and analysis
 */
@HiltViewModel
class BehaviorAnalysisViewModel @Inject constructor(
    private val trackInteractionUseCase: TrackInteractionUseCase,
    private val analyzeUserBehaviorUseCase: AnalyzeUserBehaviorUseCase,
    private val behaviorAnalysisRepository: BehaviorAnalysisRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BehaviorAnalysisUiState())
    val uiState: StateFlow<BehaviorAnalysisUiState> = _uiState.asStateFlow()
    
    private val _behaviorSummary = MutableStateFlow<BehaviorSummary?>(null)
    val behaviorSummary: StateFlow<BehaviorSummary?> = _behaviorSummary.asStateFlow()
    
    private val _preferenceUpdate = MutableStateFlow<ImplicitPreferenceUpdate?>(null)
    val preferenceUpdate: StateFlow<ImplicitPreferenceUpdate?> = _preferenceUpdate.asStateFlow()
    
    /**
     * Track user interaction
     */
    fun trackInteraction(
        userId: String,
        interactionType: InteractionType,
        activityId: String? = null,
        activityCategory: String? = null,
        sessionDuration: Int? = null,
        context: Map<String, Any> = emptyMap()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            trackInteractionUseCase(
                userId = userId,
                interactionType = interactionType,
                activityId = activityId,
                activityCategory = activityCategory,
                sessionDuration = sessionDuration,
                context = context
            ).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Analyze user behavior
     */
    fun analyzeUserBehavior(
        userId: String,
        analysisPeriodDays: Int = 7
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            analyzeUserBehaviorUseCase(
                userId = userId,
                analysisPeriodDays = analysisPeriodDays
            ).fold(
                onSuccess = { update ->
                    _preferenceUpdate.value = update
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Behavior analysis completed successfully"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    /**
     * Get behavior summary
     */
    fun getBehaviorSummary(userId: String, days: Int = 30) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            behaviorAnalysisRepository.getBehaviorSummary(userId, days).fold(
                onSuccess = { summary ->
                    _behaviorSummary.value = summary
                    _uiState.value = _uiState.value.copy(isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
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
 * UI State for Behavior Analysis
 */
data class BehaviorAnalysisUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
