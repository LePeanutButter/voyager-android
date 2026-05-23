package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.dto.AnswerItemDto
import com.voyager.tourism.data.dto.PreferenceProfilePayloadDto
import com.voyager.tourism.data.dto.QuestionnaireQuestionDto
import com.voyager.tourism.data.dto.QuestionnaireStepResponseDto
import com.voyager.tourism.data.dto.QuestionnaireSubmitResponseDto
import com.voyager.tourism.data.dto.QuestionOptionDto
import com.voyager.tourism.domain.model.PreferenceProfile
import com.voyager.tourism.domain.model.QuestionnaireAnswer
import com.voyager.tourism.domain.model.QuestionnaireQuestion
import com.voyager.tourism.domain.model.QuestionnaireStepResult
import com.voyager.tourism.domain.model.QuestionnaireSubmitResult
import com.voyager.tourism.domain.model.QuestionOption

/**
 * Maps AI questionnaire DTOs to richer domain questionnaire types.
 */
object PreferenceQuestionnaireMapper {

    /**
     * Converts a step response payload into a domain [QuestionnaireStepResult].
     */
    fun toDomain(dto: QuestionnaireStepResponseDto): QuestionnaireStepResult {
        return QuestionnaireStepResult(
            sessionId = dto.sessionId,
            stepIndex = dto.stepIndex,
            isComplete = dto.isComplete,
            derivedPrimaryCategory = dto.derivedPrimaryCategory,
            questions = dto.questions.map { toDomain(it) },
            message = dto.message
        )
    }

    /**
     * Maps a question definition DTO to [QuestionnaireQuestion].
     */
    private fun toDomain(dto: QuestionnaireQuestionDto): QuestionnaireQuestion {
        return QuestionnaireQuestion(
            id = dto.id,
            prompt = dto.prompt,
            questionType = dto.questionType,
            options = dto.options.map { toDomain(it) }
        )
    }

    /**
     * Maps a selectable option row into [QuestionOption].
     */
    private fun toDomain(dto: QuestionOptionDto): QuestionOption {
        return QuestionOption(id = dto.id, label = dto.label)
    }

    /**
     * Serializes [QuestionnaireAnswer] selections for outbound questionnaire POST bodies.
     */
    fun toDto(answers: List<QuestionnaireAnswer>): List<AnswerItemDto> {
        return answers.map {
            AnswerItemDto(
                questionId = it.questionId,
                selectedOptionIds = it.selectedOptionIds
            )
        }
    }

    /**
     * Converts the questionnaire submission response into [QuestionnaireSubmitResult].
     */
    fun toDomain(dto: QuestionnaireSubmitResponseDto): QuestionnaireSubmitResult {
        return QuestionnaireSubmitResult(
            userId = dto.userId,
            sessionId = dto.sessionId,
            primaryCategory = dto.primaryCategory,
            preferenceProfile = toProfile(dto.preferenceProfile),
            aiContextSummary = dto.aiContextSummary
        )
    }

    /**
     * Maps embedded preference profile JSON to the domain [PreferenceProfile] aggregate.
     */
    private fun toProfile(dto: PreferenceProfilePayloadDto): PreferenceProfile {
        return PreferenceProfile(
            travelCategories = dto.travelCategories,
            pace = dto.pace,
            interests = dto.interests,
            comfortLevel = dto.comfortLevel,
            notesForAi = dto.notesForAi
        )
    }
}
