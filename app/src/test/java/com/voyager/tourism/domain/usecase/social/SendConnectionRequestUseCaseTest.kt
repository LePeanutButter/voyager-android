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

class SendConnectionRequestUseCaseTest {

    private val repository = mockk<SocialRepository>()
    private lateinit var useCase: SendConnectionRequestUseCase

    @Before
    fun setup() {
        useCase = SendConnectionRequestUseCase(repository)
    }

    @Test
    fun `success returns dto`() = runTest {
        val dto = TestFixtures.connectionRequest()
        coEvery { repository.getSentRequests("t") } returns emptyList()
        coEvery { repository.sendConnectionRequest(any(), "t") } returns dto

        val r = useCase(2L, "hi", "t")
        assertTrue(r.isSuccess)
        assertEquals(dto, r.getOrThrow())
    }
}
