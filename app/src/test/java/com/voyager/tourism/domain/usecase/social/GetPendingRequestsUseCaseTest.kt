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
    fun `success returns list`() = runTest {
        val list = listOf(TestFixtures.connectionRequest())
        coEvery { repository.getPendingRequests("t") } returns list

        val r = useCase("t")
        assertTrue(r.isSuccess)
        assertEquals(list, r.getOrThrow())
    }
}
