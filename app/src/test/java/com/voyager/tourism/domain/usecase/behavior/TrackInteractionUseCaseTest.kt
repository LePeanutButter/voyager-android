package com.voyager.tourism.domain.usecase.behavior

import com.voyager.tourism.data.dto.BehaviorAnalysisSimpleResponse
import com.voyager.tourism.data.dto.InteractionType
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrackInteractionUseCaseTest {

    private val repository = mockk<BehaviorAnalysisRepository>()
    private lateinit var useCase: TrackInteractionUseCase

    @Before
    fun setup() {
        useCase = TrackInteractionUseCase(repository)
    }

    @Test
    fun `fails when userId blank`() = runTest {
        val r = useCase("", InteractionType.VIEW)
        assertTrue(r.isFailure)
    }

    @Test
    fun `adds timestamp to context when missing`() = runTest {
        val slot = slot<Map<String, Any>>()
        coEvery {
            repository.trackInteraction(any(), any(), any(), any(), any(), capture(slot))
        } returns Result.success(BehaviorAnalysisSimpleResponse(true, "ok"))

        useCase("u1", InteractionType.CLICK, context = emptyMap())

        assertTrue(slot.captured.containsKey("timestamp"))
        coVerify { repository.trackInteraction("u1", InteractionType.CLICK, any(), any(), any(), any()) }
    }

    @Test
    fun `preserves existing timestamp in context`() = runTest {
        val slot = slot<Map<String, Any>>()
        coEvery { repository.trackInteraction(any(), any(), any(), any(), any(), capture(slot)) } returns Result.success(BehaviorAnalysisSimpleResponse(true, "ok"))

        useCase("u1", InteractionType.VIEW, context = mapOf("timestamp" to 123L))

        assertEquals(123L, slot.captured["timestamp"])
    }
}
