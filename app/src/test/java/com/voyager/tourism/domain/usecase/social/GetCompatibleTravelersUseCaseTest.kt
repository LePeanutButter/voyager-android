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

class GetCompatibleTravelersUseCaseTest {

    private val repository = mockk<SocialRepository>()
    private lateinit var useCase: GetCompatibleTravelersUseCase

    @Before
    fun setup() {
        useCase = GetCompatibleTravelersUseCase(repository)
    }

    @Test
    fun `success returns travelers`() = runTest {
        val list = listOf(TestFixtures.travelerMatch())
        coEvery { repository.getCompatibleTravelers("plan-1", "tok") } returns list

        val r = useCase("plan-1", "tok")

        assertTrue(r.isSuccess)
        assertEquals(list, r.getOrThrow())
    }

    @Test
    fun `exception becomes failure`() = runTest {
        coEvery { repository.getCompatibleTravelers(any(), any()) } throws RuntimeException("net")

        val r = useCase("p", "t")

        assertTrue(r.isFailure)
        assertEquals("net", r.exceptionOrNull()?.message)
    }
}
