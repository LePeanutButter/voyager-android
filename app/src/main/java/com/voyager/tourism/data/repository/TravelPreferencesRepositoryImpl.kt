package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.AiTravelPreferencesApi
import com.voyager.tourism.data.dto.QuestionnaireStepRequestDto
import com.voyager.tourism.data.dto.QuestionnaireSubmitRequestDto
import com.voyager.tourism.data.mapper.PreferenceQuestionnaireMapper
import com.voyager.tourism.domain.model.QuestionnaireAnswer
import com.voyager.tourism.domain.model.QuestionnaireStepResult
import com.voyager.tourism.domain.model.QuestionnaireSubmitResult
import com.voyager.tourism.domain.repository.TravelPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges [AiTravelPreferencesApi] responses into domain questionnaire models.
 */
@Singleton
class TravelPreferencesRepositoryImpl @Inject constructor(
    private val api: AiTravelPreferencesApi
) : TravelPreferencesRepository {

    /** @see TravelPreferencesRepository.postQuestionnaireStep */
    override suspend fun postQuestionnaireStep(
        userId: String,
        sessionId: String?,
        answers: List<QuestionnaireAnswer>
    ): Result<QuestionnaireStepResult> {
        return try {
            val response = api.postQuestionnaireStep(
                QuestionnaireStepRequestDto(
                    userId = userId,
                    sessionId = sessionId,
                    answers = PreferenceQuestionnaireMapper.toDto(answers)
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(PreferenceQuestionnaireMapper.toDomain(body))
                }
                    ?: Result.failure(IllegalStateException("Empty response"))
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "HTTP ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see TravelPreferencesRepository.submitQuestionnaire */
    override suspend fun submitQuestionnaire(
        userId: String,
        sessionId: String,
        answers: List<QuestionnaireAnswer>
    ): Result<QuestionnaireSubmitResult> {
        return try {
            val response = api.postQuestionnaireSubmit(
                QuestionnaireSubmitRequestDto(
                    userId = userId,
                    sessionId = sessionId,
                    answers = PreferenceQuestionnaireMapper.toDto(answers)
                )
            )
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Result.success(PreferenceQuestionnaireMapper.toDomain(body))
                }
                    ?: Result.failure(IllegalStateException("Empty response"))
            } else {
                Result.failure(
                    Exception(response.errorBody()?.string() ?: "HTTP ${response.code()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
