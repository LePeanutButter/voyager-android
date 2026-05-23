package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.TravelPlanActivityDto
import com.voyager.tourism.domain.repository.TripRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class TripDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tripRepository = mockk<TripRepository>()
    private val travelPlanApi = mockk<TravelPlanApiService>()
    private lateinit var vm: TripDetailViewModel

    @Before
    fun setup() {
        vm = TripDetailViewModel(tripRepository, travelPlanApi)
    }

    @Test
    fun `loadTrip fetches trip and activities`() {
        val trip = TestFixtures.domainTrip()
        coEvery { tripRepository.getTripById("99") } returns Result.success(trip)
        coEvery { travelPlanApi.getActivities(99L) } returns ApiResponse(
            timestamp = "t",
            status = 200,
            message = "ok",
            data = listOf(TravelPlanActivityDto(name = "Museum")),
        )
        vm.loadTrip("99")
        assertEquals(trip, vm.trip.value)
        assertEquals(1, vm.activities.value.size)
    }

    @Test
    fun `non numeric id skips activities`() {
        val trip = TestFixtures.domainTrip()
        coEvery { tripRepository.getTripById("abc") } returns Result.success(trip)
        vm.loadTrip("abc")
        assertEquals(trip, vm.trip.value)
        assertTrue(vm.activities.value.isEmpty())
    }

    @Test
    fun `activities http error sets error`() {
        coEvery { tripRepository.getTripById("5") } returns Result.success(TestFixtures.domainTrip())
        coEvery { travelPlanApi.getActivities(5L) } returns ApiResponse(
            timestamp = "t",
            status = 500,
            message = "fail",
            data = null,
        )
        vm.loadTrip("5")
        assertNotNull(vm.error.value)
    }
}
