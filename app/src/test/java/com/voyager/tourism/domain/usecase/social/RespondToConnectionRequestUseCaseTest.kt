package com.voyager.tourism.domain.usecase.social

import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RespondToConnectionRequestUseCaseTest {

    private val repository = mockk<SocialRepository>()
    private lateinit var useCase: RespondToConnectionRequestUseCase

    @Before
    fun setup() {
        useCase = RespondToConnectionRequestUseCase(repository)
    }

    @Test
    fun `acceptRequest success`() = runTest {
        val dto = TestFixtures.connectionRequest(id = 1L, status = "ACCEPTED")
        coEvery { repository.acceptConnectionRequest("1", "t") } returns dto

        val result = useCase.acceptRequest("1", "t")
        assertTrue(result.isSuccess)
        assertEquals("ACCEPTED", result.getOrThrow().status)
    }

    @Test
    fun `acceptRequest failure`() = runTest {
        coEvery { repository.acceptConnectionRequest(any(), any()) } throws RuntimeException("fail")
        assertTrue(useCase.acceptRequest("1", "t").isFailure)
    }

    @Test
    fun `rejectRequest success`() = runTest {
        val dto = TestFixtures.connectionRequest(id = 2L, status = "REJECTED")
        coEvery { repository.rejectConnectionRequest("2", "t") } returns dto

        val result = useCase.rejectRequest("2", "t")
        assertTrue(result.isSuccess)
        assertEquals("REJECTED", result.getOrThrow().status)
    }
}
