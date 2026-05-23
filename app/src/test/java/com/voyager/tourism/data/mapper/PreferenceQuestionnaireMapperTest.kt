package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.dto.AnswerItemDto
import com.voyager.tourism.data.dto.PreferenceProfilePayloadDto
import com.voyager.tourism.data.dto.QuestionnaireQuestionDto
import com.voyager.tourism.data.dto.QuestionnaireStepResponseDto
import com.voyager.tourism.data.dto.QuestionnaireSubmitResponseDto
import com.voyager.tourism.data.dto.QuestionOptionDto
import com.voyager.tourism.domain.model.QuestionnaireAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreferenceQuestionnaireMapperTest {

    @Test
    fun `toDomain maps step response with questions and options`() {
        val dto = QuestionnaireStepResponseDto(
            sessionId = "s1",
            stepIndex = 2,
            isComplete = false,
            derivedPrimaryCategory = "culture",
            questions = listOf(
                QuestionnaireQuestionDto(
                    id = "q1",
                    prompt = "Pace?",
                    questionType = "single",
                    options = listOf(QuestionOptionDto("o1", "Slow")),
                ),
            ),
            message = "next",
        )

        val domain = PreferenceQuestionnaireMapper.toDomain(dto)

        assertEquals("s1", domain.sessionId)
        assertEquals(2, domain.stepIndex)
        assertEquals(false, domain.isComplete)
        assertEquals("culture", domain.derivedPrimaryCategory)
        assertEquals("next", domain.message)
        assertEquals(1, domain.questions.size)
        assertEquals("q1", domain.questions[0].id)
        assertEquals(1, domain.questions[0].options.size)
        assertEquals("o1", domain.questions[0].options[0].id)
        assertEquals("Slow", domain.questions[0].options[0].label)
    }

    @Test
    fun `toDto maps answers`() {
        val answers = listOf(
            QuestionnaireAnswer("q1", listOf("a", "b")),
            QuestionnaireAnswer("q2", emptyList()),
        )

        val dtos = PreferenceQuestionnaireMapper.toDto(answers)

        assertEquals(
            listOf(
                AnswerItemDto("q1", listOf("a", "b")),
                AnswerItemDto("q2", emptyList()),
            ),
            dtos,
        )
    }

    @Test
    fun `toDomain maps submit response and profile`() {
        val dto = QuestionnaireSubmitResponseDto(
            userId = "u",
            sessionId = "s",
            primaryCategory = "adventure",
            preferenceProfile = PreferenceProfilePayloadDto(
                travelCategories = listOf("hike"),
                pace = "fast",
                interests = listOf("nature"),
                comfortLevel = "high",
                notesForAi = "likes early flights",
            ),
            aiContextSummary = "summary",
        )

        val r = PreferenceQuestionnaireMapper.toDomain(dto)

        assertEquals("u", r.userId)
        assertEquals("s", r.sessionId)
        assertEquals("adventure", r.primaryCategory)
        assertEquals("summary", r.aiContextSummary)
        assertEquals(listOf("hike"), r.preferenceProfile.travelCategories)
        assertEquals("fast", r.preferenceProfile.pace)
        assertEquals(listOf("nature"), r.preferenceProfile.interests)
        assertEquals("high", r.preferenceProfile.comfortLevel)
        assertEquals("likes early flights", r.preferenceProfile.notesForAi)
    }

    @Test
    fun `toDomain step with empty questions`() {
        val dto = QuestionnaireStepResponseDto(
            sessionId = "s",
            stepIndex = 0,
            isComplete = true,
            questions = emptyList(),
        )
        val r = PreferenceQuestionnaireMapper.toDomain(dto)
        assertEquals(true, r.isComplete)
        assertEquals(0, r.questions.size)
        assertNull(r.derivedPrimaryCategory)
    }
}
