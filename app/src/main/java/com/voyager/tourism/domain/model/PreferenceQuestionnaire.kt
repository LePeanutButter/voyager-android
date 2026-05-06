package com.voyager.tourism.domain.model

data class QuestionOption(
    val id: String,
    val label: String
)

data class QuestionnaireQuestion(
    val id: String,
    val prompt: String,
    val questionType: String,
    val options: List<QuestionOption> = emptyList()
)

data class QuestionnaireAnswer(
    val questionId: String,
    val selectedOptionIds: List<String> = emptyList()
)

data class QuestionnaireStepResult(
    val sessionId: String,
    val stepIndex: Int,
    val isComplete: Boolean,
    val derivedPrimaryCategory: String?,
    val questions: List<QuestionnaireQuestion>,
    val message: String?
)

data class PreferenceProfile(
    val travelCategories: List<String>,
    val pace: String?,
    val interests: List<String>,
    val comfortLevel: String?,
    val notesForAi: String?
)

data class QuestionnaireSubmitResult(
    val userId: String,
    val sessionId: String,
    val primaryCategory: String,
    val preferenceProfile: PreferenceProfile,
    val aiContextSummary: String
)
