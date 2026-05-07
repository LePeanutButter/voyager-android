package com.voyager.tourism.domain.usecase.behavior

import com.voyager.tourism.data.dto.DateRange
import com.voyager.tourism.data.dto.ImplicitPreferenceUpdate
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyzeUserBehaviorUseCaseTest {

    private val repository = mockk<BehaviorAnalysisRepository>()
    private lateinit var useCase: AnalyzeUserBehaviorUseCase

    private val sampleUpdate = ImplicitPreferenceUpdate(
        userId = "u1",
        preferenceChanges = mapOf("pace" to 0.5),
        detectedPatterns = emptyList(),
        analysisPeriod = DateRange(
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 1, 7, 0, 0),
        ),
        confidenceScore = 0.9,
    )

    @Before
    fun setup() {
        useCase = AnalyzeUserBehaviorUseCase(repository)
    }

    @Test
    fun `blank user id fails`() = runTest {
        val r = useCase("  ")
        assertTrue(r.isFailure)
        assertEquals("User ID cannot be blank", r.exceptionOrNull()?.message)
    }

    @Test
    fun `period below 1 fails`() = runTest {
        val r = useCase("1", analysisPeriodDays = 0)
        assertTrue(r.isFailure)
        assertEquals("Analysis period must be between 1 and 365 days", r.exceptionOrNull()?.message)
    }

    @Test
    fun `period above 365 fails`() = runTest {
        val r = useCase("1", analysisPeriodDays = 366)
        assertTrue(r.isFailure)
    }

    @Test
    fun `delegates to repository`() = runTest {
        coEvery {
            repository.analyzeUserBehavior("42", 14, false, true)
        } returns Result.success(sampleUpdate)

        val r = useCase("42", analysisPeriodDays = 14, includePatterns = false, includePreferenceUpdates = true)

        assertTrue(r.isSuccess)
        assertEquals(sampleUpdate, r.getOrThrow())
        coVerify(exactly = 1) {
            repository.analyzeUserBehavior("42", 14, false, true)
        }
    }

    @Test
    fun `repository failure propagates`() = runTest {
        coEvery { repository.analyzeUserBehavior(any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("api"))

        val r = useCase("1")

        assertTrue(r.isFailure)
        assertEquals("api", r.exceptionOrNull()?.message)
    }
}
