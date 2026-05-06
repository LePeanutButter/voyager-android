package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateTripUseCaseTest {

    private val tripRepository = mockk<TripRepository>()
    private lateinit var useCase: UpdateTripUseCase

    @Before
    fun setup() {
        useCase = UpdateTripUseCase(tripRepository)
    }

    @Test
    fun `fails when id blank`() = runTest {
        val trip = TestFixtures.domainTrip().copy(id = "")
        val r = useCase(trip)
        assertTrue(r.isFailure)
    }

    @Test
    fun `delegates when id present`() = runTest {
        val trip = TestFixtures.domainTrip()
        coEvery { tripRepository.updateTrip(trip) } returns Result.success(trip)

        val r = useCase(trip)

        assertTrue(r.isSuccess)
    }
}
