package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuestionOptionDto(
    @Json(name = "id") val id: String,
    @Json(name = "label") val label: String
)

@JsonClass(generateAdapter = true)
data class QuestionnaireQuestionDto(
    @Json(name = "id") val id: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "question_type") val questionType: String,
    @Json(name = "options") val options: List<QuestionOptionDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AnswerItemDto(
    @Json(name = "question_id") val questionId: String,
    @Json(name = "selected_option_ids") val selectedOptionIds: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuestionnaireStepRequestDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "session_id") val sessionId: String? = null,
    @Json(name = "answers") val answers: List<AnswerItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuestionnaireStepResponseDto(
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "step_index") val stepIndex: Int,
    @Json(name = "is_complete") val isComplete: Boolean,
    @Json(name = "derived_primary_category") val derivedPrimaryCategory: String? = null,
    @Json(name = "questions") val questions: List<QuestionnaireQuestionDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class PreferenceProfilePayloadDto(
    @Json(name = "travel_categories") val travelCategories: List<String> = emptyList(),
    @Json(name = "pace") val pace: String? = null,
    @Json(name = "interests") val interests: List<String> = emptyList(),
    @Json(name = "comfort_level") val comfortLevel: String? = null,
    @Json(name = "notes_for_ai") val notesForAi: String? = null
)

@JsonClass(generateAdapter = true)
data class QuestionnaireSubmitRequestDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "answers") val answers: List<AnswerItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuestionnaireSubmitResponseDto(
    @Json(name = "user_id") val userId: String,
    @Json(name = "session_id") val sessionId: String,
    @Json(name = "primary_category") val primaryCategory: String,
    @Json(name = "preference_profile") val preferenceProfile: PreferenceProfilePayloadDto,
    @Json(name = "ai_context_summary") val aiContextSummary: String
)
