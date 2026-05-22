package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateTripUseCaseTest {

    private val repository = mockk<TripRepository>()
    private lateinit var useCase: CreateTripUseCase

    @Before
    fun setup() {
        useCase = CreateTripUseCase(repository)
    }

    @Test
    fun `success calls repository`() = runTest {
        val trip = TestFixtures.domainTrip()
        coEvery { repository.createTrip(any()) } returns Result.success(trip)

        val r = useCase(trip)
        assertTrue(r.isSuccess)
        assertEquals(trip, r.getOrThrow())
    }
}
