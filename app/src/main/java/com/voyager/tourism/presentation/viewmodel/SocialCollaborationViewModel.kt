package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.domain.model.CompatibilityMatch
import com.voyager.tourism.domain.model.SharedActivity
import com.voyager.tourism.domain.model.SharedActivityDecision
import com.voyager.tourism.domain.model.TravelActivity
import com.voyager.tourism.domain.model.TravelerConnection
import com.voyager.tourism.domain.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialCollaborationViewModel @Inject constructor(
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialCollaborationUiState())
    val uiState: StateFlow<SocialCollaborationUiState> = _uiState.asStateFlow()

    fun loadConnections(userId: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isLoadingConnections = true, errorMessage = null) }
        socialRepository.getConnections(userId)
            .onSuccess { list -> _uiState.update { it.copy(isLoadingConnections = false, connections = list) } }
            .onFailure { error -> _uiState.update { it.copy(isLoadingConnections = false, errorMessage = error.message) } }
    }

    fun loadActivities(travelPlanId: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isLoadingActivities = true, errorMessage = null) }
        socialRepository.getTravelPlanActivities(travelPlanId)
            .onSuccess { list -> _uiState.update { it.copy(isLoadingActivities = false, activities = list) } }
            .onFailure { error -> _uiState.update { it.copy(isLoadingActivities = false, errorMessage = error.message) } }
    }

    fun shareActivity(activityId: Long, receiverId: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isSubmittingShare = true, errorMessage = null, successMessage = null) }
        socialRepository.shareActivity(activityId, receiverId)
            .onSuccess { shared ->
                _uiState.update { state ->
                    state.copy(
                        isSubmittingShare = false,
                        successMessage = "Activity shared successfully",
                        sharedActivities = listOf(shared) + state.sharedActivities.filterNot { it.id == shared.id }
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isSubmittingShare = false, errorMessage = error.message) }
            }
    }

    fun resolveSharedActivity(sharedActivityId: Long, decision: SharedActivityDecision) = viewModelScope.launch {
        _uiState.update { it.copy(isResolvingSharedActivity = true, errorMessage = null, successMessage = null) }
        socialRepository.resolveSharedActivity(sharedActivityId, decision)
            .onSuccess { updated ->
                _uiState.update { state ->
                    val existing = state.sharedActivities.any { it.id == updated.id }
                    state.copy(
                        isResolvingSharedActivity = false,
                        successMessage = "Shared activity updated",
                        sharedActivities = if (existing) {
                            state.sharedActivities.map { if (it.id == updated.id) updated else it }
                        } else {
                            listOf(updated) + state.sharedActivities
                        }
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isResolvingSharedActivity = false, errorMessage = error.message) }
            }
    }

    fun loadCompatibilityMatches(
        destination: String,
        startDate: String,
        endDate: String,
        interests: List<String>
    ) = viewModelScope.launch {
        _uiState.update { it.copy(isLoadingMatches = true, errorMessage = null) }
        socialRepository.getCompatibilityMatches(destination, startDate, endDate, interests)
            .onSuccess { matches ->
                _uiState.update {
                    it.copy(
                        isLoadingMatches = false,
                        allMatches = matches,
                        filteredMatches = applyInterestFilter(matches, it.selectedInterests)
                    )
                }
            }
            .onFailure { error ->
                _uiState.update { it.copy(isLoadingMatches = false, errorMessage = error.message) }
            }
    }

    fun applyInterestsFilter(interests: Set<String>) {
        _uiState.update {
            it.copy(
                selectedInterests = interests,
                filteredMatches = applyInterestFilter(it.allMatches, interests)
            )
        }
    }

    fun trackSharedActivity(sharedActivity: SharedActivity) {
        _uiState.update { state ->
            state.copy(
                sharedActivities = listOf(sharedActivity) + state.sharedActivities.filterNot { it.id == sharedActivity.id }
            )
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun applyInterestFilter(matches: List<CompatibilityMatch>, interests: Set<String>): List<CompatibilityMatch> {
        if (interests.isEmpty()) return matches
        return matches.filter { match ->
            match.matchedInterests.any { it in interests }
        }
    }
}

data class SocialCollaborationUiState(
    val connections: List<TravelerConnection> = emptyList(),
    val activities: List<TravelActivity> = emptyList(),
    val sharedActivities: List<SharedActivity> = emptyList(),
    val allMatches: List<CompatibilityMatch> = emptyList(),
    val filteredMatches: List<CompatibilityMatch> = emptyList(),
    val selectedInterests: Set<String> = emptySet(),
    val isLoadingConnections: Boolean = false,
    val isLoadingActivities: Boolean = false,
    val isSubmittingShare: Boolean = false,
    val isResolvingSharedActivity: Boolean = false,
    val isLoadingMatches: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
