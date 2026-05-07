package com.voyager.tourism.domain.usecase.social

import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
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
    fun `fails when pending request already exists for recipient`() = runTest {
        coEvery { repository.getSentRequests("tok") } returns listOf(
            TestFixtures.connectionRequest(recipientId = 5L, status = "PENDING"),
        )

        val r = useCase(5L, "hi", "tok")

        assertTrue(r.isFailure)
        assertTrue(
            r.exceptionOrNull()?.message?.contains("already pending", ignoreCase = true) == true,
        )
    }

    @Test
    fun `success sends new request`() = runTest {
        coEvery { repository.getSentRequests("tok") } returns emptyList()
        val created = TestFixtures.connectionRequest(id = 99L)
        val body = slot<SendConnectionRequestDto>()
        coEvery { repository.sendConnectionRequest(capture(body), "tok") } returns created

        val r = useCase(7L, "hello", "tok")

        assertTrue(r.isSuccess)
        assertEquals(created, r.getOrThrow())
        assertEquals(7L, body.captured.recipientId)
        assertEquals("hello", body.captured.message)
    }

    @Test
    fun `exception becomes failure`() = runTest {
        coEvery { repository.getSentRequests(any()) } throws RuntimeException("down")

        val r = useCase(1L, null, "t")

        assertTrue(r.isFailure)
        assertEquals("down", r.exceptionOrNull()?.message)
    }
}
