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

object PreferenceQuestionnaireMapper {

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

    private fun toDomain(dto: QuestionnaireQuestionDto): QuestionnaireQuestion {
        return QuestionnaireQuestion(
            id = dto.id,
            prompt = dto.prompt,
            questionType = dto.questionType,
            options = dto.options.map { toDomain(it) }
        )
    }

    private fun toDomain(dto: QuestionOptionDto): QuestionOption {
        return QuestionOption(id = dto.id, label = dto.label)
    }

    fun toDto(answers: List<QuestionnaireAnswer>): List<AnswerItemDto> {
        return answers.map {
            AnswerItemDto(
                questionId = it.questionId,
                selectedOptionIds = it.selectedOptionIds
            )
        }
    }

    fun toDomain(dto: QuestionnaireSubmitResponseDto): QuestionnaireSubmitResult {
        return QuestionnaireSubmitResult(
            userId = dto.userId,
            sessionId = dto.sessionId,
            primaryCategory = dto.primaryCategory,
            preferenceProfile = toProfile(dto.preferenceProfile),
            aiContextSummary = dto.aiContextSummary
        )
    }

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
