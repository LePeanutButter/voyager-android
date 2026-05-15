package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.domain.model.TripStatus
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateTripUseCaseTest {

    private val tripRepository = mockk<TripRepository>()
    private lateinit var useCase: CreateTripUseCase

    @Before
    fun setup() {
        useCase = CreateTripUseCase(tripRepository)
    }

    @Test
    fun `fails when title blank`() = runTest {
        val trip = TestFixtures.domainTrip().copy(title = " ")
        val r = useCase(trip)
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull()?.message?.contains("title") == true)
    }

    @Test
    fun `fails when user id blank`() = runTest {
        val trip = TestFixtures.domainTrip().copy(userId = "")
        val r = useCase(trip)
        assertTrue(r.isFailure)
    }

    @Test
    fun `fails when destination name blank`() = runTest {
        val trip = TestFixtures.domainTrip().copy(
            destination = TestFixtures.domainTrip().destination.copy(name = ""),
        )
        val r = useCase(trip)
        assertTrue(r.isFailure)
    }

    @Test
    fun `fails when start not before end`() = runTest {
        val trip = TestFixtures.domainTrip().copy(startDate = 3000L, endDate = 1000L)
        val r = useCase(trip)
        assertTrue(r.isFailure)
    }

    @Test
    fun `fails when budget not positive`() = runTest {
        val trip = TestFixtures.domainTrip().copy(budget = 0.0)
        val r = useCase(trip)
        assertTrue(r.isFailure)
    }

    @Test
    fun `fails when travelers not positive`() = runTest {
        val trip = TestFixtures.domainTrip().copy(travelers = 0)
        val r = useCase(trip)
        assertTrue(r.isFailure)
    }

    @Test
    fun `success sets planning and timestamps`() = runTest {
        val trip = TestFixtures.domainTrip()
        val cap = slot<Trip>()
        coEvery { tripRepository.createTrip(capture(cap)) } answers {
            Result.success(cap.captured)
        }

        val r = useCase(trip)

        assertTrue(r.isSuccess)
        val created = r.getOrNull()!!
        assertEquals(TripStatus.ACTIVE, created.status)
        assertTrue(created.createdAt > 0)
        assertTrue(created.updatedAt > 0)
    }
}
