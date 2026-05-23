package com.voyager.tourism.domain.repository

import com.voyager.tourism.domain.model.QuestionnaireAnswer
import com.voyager.tourism.domain.model.QuestionnaireStepResult
import com.voyager.tourism.domain.model.QuestionnaireSubmitResult

/**
 * Persists adaptive travel preference questionnaires through the AI service.
 */
interface TravelPreferencesRepository {

    /**
     * Submits answers for the current questionnaire step and returns validation plus the next prompt payload.
     */
    suspend fun postQuestionnaireStep(
        userId: String,
        sessionId: String?,
        answers: List<QuestionnaireAnswer>
    ): Result<QuestionnaireStepResult>

    /**
     * Finalizes the questionnaire for the given session identifier.
     */
    suspend fun submitQuestionnaire(
        userId: String,
        sessionId: String,
        answers: List<QuestionnaireAnswer>
    ): Result<QuestionnaireSubmitResult>
}
