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
        val dto = TestFixtures.connectionRequest(status = "ACCEPTED")
        coEvery { repository.acceptConnectionRequest("9", "tok") } returns dto

        val r = useCase.acceptRequest("9", "tok")

        assertTrue(r.isSuccess)
        assertEquals(dto, r.getOrThrow())
    }

    @Test
    fun `acceptRequest failure`() = runTest {
        coEvery { repository.acceptConnectionRequest(any(), any()) } throws RuntimeException("no")

        val r = useCase.acceptRequest("1", "t")

        assertTrue(r.isFailure)
        assertEquals("no", r.exceptionOrNull()?.message)
    }

    @Test
    fun `rejectRequest success`() = runTest {
        val dto = TestFixtures.connectionRequest(status = "REJECTED")
        coEvery { repository.rejectConnectionRequest("9", "tok") } returns dto

        val r = useCase.rejectRequest("9", "tok")

        assertTrue(r.isSuccess)
        assertEquals(dto, r.getOrThrow())
    }

    @Test
    fun `rejectRequest failure`() = runTest {
        coEvery { repository.rejectConnectionRequest(any(), any()) } throws RuntimeException("e")

        val r = useCase.rejectRequest("1", "t")

        assertTrue(r.isFailure)
    }
}
