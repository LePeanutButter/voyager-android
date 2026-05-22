package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.repository.TripRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteTripUseCaseTest {

    private val repository = mockk<TripRepository>()
    private lateinit var useCase: DeleteTripUseCase

    @Before
    fun setup() {
        useCase = DeleteTripUseCase(repository)
    }

    @Test
    fun `fails when tripId blank`() = runTest {
        assertTrue(useCase("").isFailure)
    }

    @Test
    fun `success calls repository`() = runTest {
        coEvery { repository.deleteTrip("1") } returns Result.success(Unit)
        assertTrue(useCase("1").isSuccess)
    }

    @Test
    fun `failure returns error`() = runTest {
        coEvery { repository.deleteTrip(any()) } returns Result.failure(Exception("fail"))
        val r = useCase("1")
        assertTrue(r.isFailure)
        assertEquals("fail", r.exceptionOrNull()?.message)
    }
}
