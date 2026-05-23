package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.TripRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTripsUseCaseTest {

    private val repository = mockk<TripRepository>()
    private val preferencesManager = mockk<PreferencesManager>()
    private lateinit var useCase: GetTripsUseCase

    @Before
    fun setup() {
        useCase = GetTripsUseCase(repository, preferencesManager)
    }

    @Test
    fun `fails when userId blank`() = runTest {
        assertTrue(useCase("").isFailure)
    }

    @Test
    fun `success returns list`() = runTest {
        val list = listOf(TestFixtures.domainTrip())
        coEvery { repository.getUserTrips("1") } returns Result.success(list)
        val r = useCase("1")
        assertTrue(r.isSuccess)
        assertEquals(list, r.getOrThrow())
    }

    @Test
    fun `invoke without arguments returns current user trips`() = runTest {
        val list = listOf(TestFixtures.domainTrip())
        every { preferencesManager.getCurrentUserId() } returns "1"
        coEvery { repository.getUserTrips("1") } returns Result.success(list)
        val r = useCase()
        assertTrue(r.isSuccess)
        assertEquals(list, r.getOrThrow())
    }

    @Test
    fun `invoke without arguments fails when no session`() = runTest {
        every { preferencesManager.getCurrentUserId() } returns null
        val r = useCase()
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalStateException)
    }
}
