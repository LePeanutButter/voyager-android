package com.voyager.tourism.domain.usecase.behavior

import com.voyager.tourism.data.dto.BehaviorAnalysisSimpleResponse
import com.voyager.tourism.data.dto.InteractionType
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
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
    fun `fails when user id blank`() = runTest {
        val r = useCase(" ", InteractionType.VIEW)
        assertTrue(r.isFailure)
    }

    @Test
    fun `adds timestamp to context when missing`() = runTest {
        val ctx = slot<Map<String, Any>>()
        coEvery {
            repository.trackInteraction(
                "1",
                InteractionType.CLICK,
                null,
                null,
                null,
                capture(ctx),
            )
        } returns Result.success(BehaviorAnalysisSimpleResponse(true, "ok"))

        val r = useCase("1", InteractionType.CLICK, context = emptyMap())

        assertTrue(r.isSuccess)
        assertTrue(ctx.captured.containsKey("timestamp"))
    }
}
