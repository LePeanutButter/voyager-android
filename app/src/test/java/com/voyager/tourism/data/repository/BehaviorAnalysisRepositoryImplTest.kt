package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.BehaviorAnalysisApi
import com.voyager.tourism.data.dto.BehaviorAnalysisSimpleResponse
import com.voyager.tourism.data.dto.BehaviorSummary
import com.voyager.tourism.data.dto.BehaviorTrackingRequest
import com.voyager.tourism.data.dto.InteractionType
import com.voyager.tourism.data.dto.ImplicitPreferenceUpdate
import com.voyager.tourism.data.local.PreferencesManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.time.LocalDateTime

class BehaviorAnalysisRepositoryImplTest {

    private val api = mockk<BehaviorAnalysisApi>()
    private val prefs = mockk<PreferencesManager>()
    private lateinit var repo: BehaviorAnalysisRepositoryImpl

    @Before
    fun setup() {
        every { prefs.getAuthToken() } returns "jwt"
        repo = BehaviorAnalysisRepositoryImpl(api, prefs)
    }

    @Test
    fun `trackInteraction no token fails`() = runTest {
        every { prefs.getAuthToken() } returns null
        val r = repo.trackInteraction("1", InteractionType.VIEW, null, null, null, emptyMap())
        assertTrue(r.isFailure)
        assertEquals("Not authenticated", r.exceptionOrNull()?.message)
    }

    @Test
    fun `trackInteraction success and http error`() = runTest {
        coEvery { api.trackUserBehavior(any(), "Bearer jwt") } returns Response.success(
            BehaviorAnalysisSimpleResponse(true, "ok"),
        )
        val ok = repo.trackInteraction("1", InteractionType.CLICK, "a", "c", 5, mapOf("k" to 1))
        assertTrue(ok.isSuccess)

        coEvery { api.trackUserBehavior(any(), any()) } returns Response.error(500, "".toResponseBody())
        val bad = repo.trackInteraction("1", InteractionType.VIEW, null, null, null, emptyMap())
        assertTrue(bad.isFailure)
    }

    @Test
    fun `analyzeUserBehavior success`() = runTest {
        val body = ImplicitPreferenceUpdate(
            userId = "u",
            preferenceChanges = emptyMap(),
            detectedPatterns = emptyList(),
            analysisPeriod = com.voyager.tourism.data.dto.DateRange(
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 2, 0, 0),
            ),
            confidenceScore = 1.0,
        )
        coEvery { api.analyzeUserBehavior(any(), any()) } returns Response.success(body)
        val r = repo.analyzeUserBehavior("u", 7, true, true)
        assertTrue(r.isSuccess)
        assertEquals(body, r.getOrThrow())
    }

    @Test
    fun `getBehaviorSummary success`() = runTest {
        val summary = BehaviorSummary(
            userId = "u",
            analysisPeriodDays = 30,
            totalInteractions = 1,
            interactionBreakdown = emptyMap(),
            categoryBreakdown = emptyMap(),
            recentPatterns = emptyList(),
            lastAnalysis = null,
        )
        coEvery { api.getBehaviorSummary("u", 14, "Bearer jwt") } returns Response.success(summary)
        val r = repo.getBehaviorSummary("u", 14)
        assertTrue(r.isSuccess)
    }

    @Test
    fun `batchTrackInteractions and getDetectedPatterns and clearUserBehaviorData`() = runTest {
        coEvery { api.batchTrackBehavior(any(), any()) } returns Response.success(
            BehaviorAnalysisSimpleResponse(true, "batch"),
        )
        assertTrue(
            repo.batchTrackInteractions(
                listOf(BehaviorTrackingRequest("u", InteractionType.VIEW)),
            ).isSuccess,
        )

        coEvery { api.getDetectedPatterns("u", 7, any()) } returns Response.success(mapOf("a" to 1))
        assertTrue(repo.getDetectedPatterns("u", 7).isSuccess)

        coEvery { api.clearUserBehaviorData("u", any()) } returns Response.success(
            BehaviorAnalysisSimpleResponse(true, "cleared"),
        )
        assertTrue(repo.clearUserBehaviorData("u").isSuccess)
    }
}
