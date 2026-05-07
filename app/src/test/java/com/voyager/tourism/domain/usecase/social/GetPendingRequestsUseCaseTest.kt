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

class GetPendingRequestsUseCaseTest {

    private val repository = mockk<SocialRepository>()
    private lateinit var useCase: GetPendingRequestsUseCase

    @Before
    fun setup() {
        useCase = GetPendingRequestsUseCase(repository)
    }

    @Test
    fun `success returns requests`() = runTest {
        val list = listOf(TestFixtures.connectionRequest())
        coEvery { repository.getPendingRequests("tok") } returns list

        val r = useCase("tok")

        assertTrue(r.isSuccess)
        assertEquals(list, r.getOrThrow())
    }

    @Test
    fun `exception becomes failure`() = runTest {
        coEvery { repository.getPendingRequests(any()) } throws IllegalStateException("x")

        val r = useCase("t")

        assertTrue(r.isFailure)
        assertEquals("x", r.exceptionOrNull()?.message)
    }
}
