package com.voyager.tourism.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyager.tourism.domain.model.QuestionnaireAnswer
import com.voyager.tourism.domain.model.QuestionnaireQuestion
import com.voyager.tourism.domain.model.QuestionnaireSubmitResult
import com.voyager.tourism.domain.repository.TravelPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TravelPreferencesViewModel @Inject constructor(
    private val repository: TravelPreferencesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    private val _questions = MutableStateFlow<List<QuestionnaireQuestion>>(emptyList())
    val questions: StateFlow<List<QuestionnaireQuestion>> = _questions.asStateFlow()

    private val _stepIndex = MutableStateFlow(0)
    val stepIndex: StateFlow<Int> = _stepIndex.asStateFlow()

    private val _derivedCategory = MutableStateFlow<String?>(null)
    val derivedCategory: StateFlow<String?> = _derivedCategory.asStateFlow()

    private val _awaitingFinalSubmit = MutableStateFlow(false)
    val awaitingFinalSubmit: StateFlow<Boolean> = _awaitingFinalSubmit.asStateFlow()

    private val _submitResult = MutableStateFlow<QuestionnaireSubmitResult?>(null)
    val submitResult: StateFlow<QuestionnaireSubmitResult?> = _submitResult.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun startOrRefresh(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _awaitingFinalSubmit.value = false
            _submitResult.value = null
            repository.postQuestionnaireStep(
                userId = userId,
                sessionId = null,
                answers = emptyList()
            ).onSuccess { step ->
                _sessionId.value = step.sessionId
                _questions.value = step.questions
                _stepIndex.value = step.stepIndex
                _derivedCategory.value = step.derivedPrimaryCategory
                _awaitingFinalSubmit.value = step.isComplete
            }.onFailure { e ->
                _error.value = e.message ?: "Error al cargar el cuestionario"
            }
            _isLoading.value = false
        }
    }

    /**
     * @param answers One entry per current question, single-select option id.
     */
    fun sendStep(userId: String, answers: List<QuestionnaireAnswer>) {
        val sid = _sessionId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.postQuestionnaireStep(
                userId = userId,
                sessionId = sid,
                answers = answers
            ).onSuccess { step ->
                _sessionId.value = step.sessionId
                _questions.value = step.questions
                _stepIndex.value = step.stepIndex
                _derivedCategory.value = step.derivedPrimaryCategory
                _awaitingFinalSubmit.value = step.isComplete
            }.onFailure { e ->
                _error.value = e.message ?: "Error al enviar respuestas"
            }
            _isLoading.value = false
        }
    }

    fun finalize(userId: String) {
        val sid = _sessionId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.submitQuestionnaire(
                userId = userId,
                sessionId = sid,
                answers = emptyList()
            ).onSuccess { res ->
                _submitResult.value = res
            }.onFailure { e ->
                _error.value = e.message ?: "Error al finalizar"
            }
            _isLoading.value = false
        }
    }
}
