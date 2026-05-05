package com.voyager.tourism.domain.repository

import com.voyager.tourism.domain.model.QuestionnaireAnswer
import com.voyager.tourism.domain.model.QuestionnaireStepResult
import com.voyager.tourism.domain.model.QuestionnaireSubmitResult

interface TravelPreferencesRepository {

    suspend fun postQuestionnaireStep(
        userId: String,
        sessionId: String?,
        answers: List<QuestionnaireAnswer>
    ): Result<QuestionnaireStepResult>

    suspend fun submitQuestionnaire(
        userId: String,
        sessionId: String,
        answers: List<QuestionnaireAnswer>
    ): Result<QuestionnaireSubmitResult>
}
