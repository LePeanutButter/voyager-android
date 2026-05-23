package com.voyager.tourism.domain.model

/**
 * Selectable option shown for a single questionnaire question.
 */
data class QuestionOption(
    val id: String,
    val label: String
)

/**
 * One step in the travel preference questionnaire, including prompt and choices.
 */
data class QuestionnaireQuestion(
    val id: String,
    val prompt: String,
    val questionType: String,
    val options: List<QuestionOption> = emptyList()
)

/**
 * User selections for a given question in a questionnaire session.
 */
data class QuestionnaireAnswer(
    val questionId: String,
    val selectedOptionIds: List<String> = emptyList()
)

/**
 * Result of submitting answers for one questionnaire step, including the next questions or completion flag.
 */
data class QuestionnaireStepResult(
    val sessionId: String,
    val stepIndex: Int,
    val isComplete: Boolean,
    val derivedPrimaryCategory: String?,
    val questions: List<QuestionnaireQuestion>,
    val message: String?
)

/**
 * Normalized travel preference dimensions derived from questionnaire answers for downstream AI use.
 */
data class PreferenceProfile(
    val travelCategories: List<String>,
    val pace: String?,
    val interests: List<String>,
    val comfortLevel: String?,
    val notesForAi: String?
)

/**
 * Outcome of completing the full questionnaire, including profile and AI-oriented summary text.
 */
data class QuestionnaireSubmitResult(
    val userId: String,
    val sessionId: String,
    val primaryCategory: String,
    val preferenceProfile: PreferenceProfile,
    val aiContextSummary: String
)
