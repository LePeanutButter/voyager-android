package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTripsUseCaseTest {

    private val tripRepository = mockk<TripRepository>()
    private val preferencesManager = mockk<PreferencesManager>()

    private lateinit var useCase: GetTripsUseCase

    @Before
    fun setup() {
        useCase = GetTripsUseCase(tripRepository, preferencesManager)
    }

    @Test
    fun `invoke without user id fails`() = runTest {
        coEvery { preferencesManager.getCurrentUserId() } returns null

        val r = useCase()

        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `invoke without args uses stored user id`() = runTest {
        coEvery { preferencesManager.getCurrentUserId() } returns "7"
        val trips = listOf(TestFixtures.domainTrip())
        coEvery { tripRepository.getUserTrips("7") } returns Result.success(trips)

        val r = useCase()

        assertTrue(r.isSuccess)
        assertEquals(1, r.getOrNull()?.size)
    }

    @Test
    fun `invoke with user id delegates to repository`() = runTest {
        coEvery { tripRepository.getUserTrips("1") } returns Result.success(emptyList())

        val r = useCase("1")

        assertTrue(r.isSuccess)
        assertTrue(r.getOrNull().isNullOrEmpty())
    }

    @Test
    fun `invoke catches exceptions`() = runTest {
        coEvery { tripRepository.getUserTrips(any()) } throws RuntimeException("db")

        val r = useCase("1")

        assertTrue(r.isFailure)
        assertEquals("db", r.exceptionOrNull()?.message)
    }
}
