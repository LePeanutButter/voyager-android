package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.BehaviorAnalysisSimpleResponse
import com.voyager.tourism.data.dto.BehaviorSummary
import com.voyager.tourism.data.dto.DateRange
import com.voyager.tourism.data.dto.ImplicitPreferenceUpdate
import com.voyager.tourism.data.dto.InteractionType
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import com.voyager.tourism.domain.usecase.behavior.AnalyzeUserBehaviorUseCase
import com.voyager.tourism.domain.usecase.behavior.TrackInteractionUseCase
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class BehaviorAnalysisViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trackInteraction = mockk<TrackInteractionUseCase>()
    private val analyzeBehavior = mockk<AnalyzeUserBehaviorUseCase>()
    private val repository = mockk<BehaviorAnalysisRepository>()
    private lateinit var vm: BehaviorAnalysisViewModel

    private val preferenceUpdate = ImplicitPreferenceUpdate(
        userId = "u",
        preferenceChanges = emptyMap(),
        detectedPatterns = emptyList(),
        analysisPeriod = DateRange(
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 2, 0, 0),
        ),
        confidenceScore = 1.0,
    )

    private val summary = BehaviorSummary(
        userId = "u",
        analysisPeriodDays = 30,
        totalInteractions = 3,
        interactionBreakdown = mapOf("VIEW" to 2),
        categoryBreakdown = mapOf("cat" to 1),
        recentPatterns = emptyList(),
        lastAnalysis = null,
    )

    @Before
    fun setup() {
        vm = BehaviorAnalysisViewModel(trackInteraction, analyzeBehavior, repository)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `trackInteraction success`() = runTest {
        coEvery {
            trackInteraction("1", InteractionType.VIEW, null, null, null, any())
        } returns Result.success(BehaviorAnalysisSimpleResponse(true, "ok"))

        vm.trackInteraction("1", InteractionType.VIEW)
        advanceUntilIdle()

        assertEquals("ok", vm.uiState.value.successMessage)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `trackInteraction failure`() = runTest {
        coEvery {
            trackInteraction(any(), any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("fail"))

        vm.trackInteraction("1", InteractionType.CLICK)
        advanceUntilIdle()

        assertEquals("fail", vm.uiState.value.error)
    }

    @Test
    fun `analyzeUserBehavior success updates preference flow`() = runTest {
        coEvery { analyzeBehavior("u", 14) } returns Result.success(preferenceUpdate)

        vm.analyzeUserBehavior("u", analysisPeriodDays = 14)
        advanceUntilIdle()

        assertEquals(preferenceUpdate, vm.preferenceUpdate.value)
        assertTrue(vm.uiState.value.successMessage!!.contains("completed", ignoreCase = true))
    }

    @Test
    fun `analyzeUserBehavior failure`() = runTest {
        coEvery { analyzeBehavior(any(), any()) } returns Result.failure(IllegalArgumentException("bad"))

        vm.analyzeUserBehavior("u")
        advanceUntilIdle()

        assertEquals("bad", vm.uiState.value.error)
    }

    @Test
    fun `getBehaviorSummary success`() = runTest {
        coEvery { repository.getBehaviorSummary("u", 7) } returns Result.success(summary)

        vm.getBehaviorSummary("u", days = 7)
        advanceUntilIdle()

        assertEquals(summary, vm.behaviorSummary.value)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `getBehaviorSummary failure`() = runTest {
        coEvery { repository.getBehaviorSummary(any(), any()) } returns Result.failure(RuntimeException("x"))

        vm.getBehaviorSummary("u")
        advanceUntilIdle()

        assertEquals("x", vm.uiState.value.error)
    }

    @Test
    fun `clearError and clearSuccessMessage`() = runTest {
        coEvery { trackInteraction(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("e"))
        vm.trackInteraction("1", InteractionType.VIEW)
        advanceUntilIdle()
        vm.clearError()
        assertNull(vm.uiState.value.error)

        coEvery { trackInteraction(any(), any(), any(), any(), any(), any()) } returns
            Result.success(BehaviorAnalysisSimpleResponse(true, "done"))
        vm.trackInteraction("1", InteractionType.VIEW)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.successMessage)
        vm.clearSuccessMessage()
        assertNull(vm.uiState.value.successMessage)
    }
}
