package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.usecase.trip.CreateTripUseCase
import com.voyager.tourism.domain.usecase.trip.DeleteTripUseCase
import com.voyager.tourism.domain.usecase.trip.GetTripsUseCase
import com.voyager.tourism.domain.usecase.trip.UpdateTripUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class TripViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createTrip = mockk<CreateTripUseCase>()
    private val getTrips = mockk<GetTripsUseCase>()
    private val updateTrip = mockk<UpdateTripUseCase>()
    private val deleteTrip = mockk<DeleteTripUseCase>()
    private val preferences = mockk<PreferencesManager>()
    private lateinit var vm: TripViewModel

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        vm = TripViewModel(createTrip, getTrips, updateTrip, deleteTrip, preferences)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
    }

    @Test
    fun `loadTripsForCurrentUser without id sets error`() = runTest {
        coEvery { preferences.getCurrentUserId() } returns null
        vm.loadTripsForCurrentUser()
        advanceUntilIdle()
        assertNotNull(vm.errorMessage.value)
    }

    @Test
    fun `loadTrips success`() = runTest {
        val trip = TestFixtures.domainTrip()
        coEvery { getTrips("1") } returns Result.success(listOf(trip))
        vm.loadTrips("1")
        advanceUntilIdle()
        assertEquals(1, vm.trips.value.size)
    }

    @Test
    fun `createTrip appends list`() = runTest {
        val trip = TestFixtures.domainTrip()
        coEvery { createTrip(trip) } returns Result.success(trip)
        vm.createTrip(trip)
        advanceUntilIdle()
        assertEquals(1, vm.trips.value.size)
        assertNotNull(vm.successMessage.value)
    }

    @Test
    fun `updateTrip replaces item`() = runTest {
        val t1 = TestFixtures.domainTrip()
        val t2 = t1.copy(title = "New")
        coEvery { getTrips(any()) } returns Result.success(listOf(t1))
        vm.loadTrips("1")
        advanceUntilIdle()
        coEvery { updateTrip(t2) } returns Result.success(t2)
        vm.updateTrip(t2)
        advanceUntilIdle()
        assertEquals("New", vm.trips.value.first().title)
    }

    @Test
    fun `deleteTrip removes item`() = runTest {
        val t1 = TestFixtures.domainTrip()
        coEvery { getTrips(any()) } returns Result.success(listOf(t1))
        vm.loadTrips("1")
        advanceUntilIdle()
        coEvery { deleteTrip("trip-1") } returns Result.success(Unit)
        vm.deleteTrip("trip-1")
        advanceUntilIdle()
        assertTrue(vm.trips.value.isEmpty())
    }

    @Test
    fun `select and clear trip`() {
        val t = TestFixtures.domainTrip()
        vm.selectTrip(t)
        assertEquals(t, vm.selectedTrip.value)
        vm.clearSelectedTrip()
        assertNull(vm.selectedTrip.value)
    }

    @Test
    fun `clearMessages`() {
        vm.clearMessages()
        assertNull(vm.errorMessage.value)
        assertNull(vm.successMessage.value)
    }
}
