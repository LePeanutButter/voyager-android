package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.AiTravelPreferencesApi
import com.voyager.tourism.data.dto.QuestionnaireStepResponseDto
import com.voyager.tourism.data.dto.QuestionnaireSubmitResponseDto
import com.voyager.tourism.data.dto.PreferenceProfilePayloadDto
import com.voyager.tourism.domain.model.QuestionnaireAnswer
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class TravelPreferencesRepositoryImplTest {

    private val api = mockk<AiTravelPreferencesApi>()
    private lateinit var repo: TravelPreferencesRepositoryImpl

    @Before
    fun setup() {
        repo = TravelPreferencesRepositoryImpl(api)
    }

    @Test
    fun `postQuestionnaireStep success maps domain`() = runTest {
        val dto = QuestionnaireStepResponseDto(
            sessionId = "s",
            stepIndex = 1,
            isComplete = false,
            questions = emptyList(),
        )
        coEvery { api.postQuestionnaireStep(any()) } returns Response.success(dto)
        val r = repo.postQuestionnaireStep("u", null, emptyList())
        assertTrue(r.isSuccess)
        assertEquals("s", r.getOrThrow().sessionId)
    }

    @Test
    fun `postQuestionnaireStep empty body fails`() = runTest {
        coEvery { api.postQuestionnaireStep(any()) } returns Response.success(null)
        val r = repo.postQuestionnaireStep("u", null, emptyList())
        assertTrue(r.isFailure)
    }

    @Test
    fun `postQuestionnaireStep http error`() = runTest {
        coEvery { api.postQuestionnaireStep(any()) } returns Response.error(
            400,
            "bad".toResponseBody(),
        )
        val r = repo.postQuestionnaireStep("u", "s", listOf(QuestionnaireAnswer("q", listOf("a"))))
        assertTrue(r.isFailure)
    }

    @Test
    fun `submitQuestionnaire success`() = runTest {
        val dto = QuestionnaireSubmitResponseDto(
            userId = "u",
            sessionId = "s",
            primaryCategory = "cat",
            preferenceProfile = PreferenceProfilePayloadDto(),
            aiContextSummary = "ai",
        )
        coEvery { api.postQuestionnaireSubmit(any()) } returns Response.success(dto)
        val r = repo.submitQuestionnaire("u", "s", emptyList())
        assertTrue(r.isSuccess)
        assertEquals("ai", r.getOrThrow().aiContextSummary)
    }
}
