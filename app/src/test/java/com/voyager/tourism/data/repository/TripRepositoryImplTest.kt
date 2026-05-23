package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.database.dao.TripDao
import com.voyager.tourism.data.database.entity.TripEntity
import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.PagedResponseTravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanStatus
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.mapper.TripMapper
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.model.TripStatus
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TripRepositoryImplTest {

    private val travelPlanApi = mockk<TravelPlanApiService>()
    private val tripDao = mockk<TripDao>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>()
    private val tripMapper = TripMapper()
    private lateinit var repo: TripRepositoryImpl

    private fun <T> apiOk(data: T?, status: Int = 200, msg: String = "OK") =
        ApiResponse("t", status, msg, data, null, null)

    private fun entity(id: String = "10") = TripEntity(
        id = id,
        userId = "42",
        title = "Local",
        description = "",
        destination = "X",
        startDate = 1L,
        endDate = 2L,
        budget = 1.0,
        travelers = 1,
        status = TripStatus.PLANNING.name,
        itinerary = null,
        accommodations = null,
        activities = null,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Before
    fun setup() {
        repo = TripRepositoryImpl(travelPlanApi, tripDao, tripMapper, preferencesManager)
    }

    @Test
    fun `getUserTrips uses dao when not authenticated`() = runTest {
        every { preferencesManager.getAuthToken() } returns null
        coEvery { tripDao.getTripsByUserId("42") } returns listOf(entity())
        val r = repo.getUserTrips("42")
        assertTrue(r.isSuccess)
        assertEquals(1, r.getOrThrow().size)
    }

    @Test
    fun `getUserTrips fetches remote when authenticated`() = runTest {
        every { preferencesManager.getAuthToken() } returns "tok"
        val dto = TestFixtures.travelPlanDto(id = 7L)
        coEvery { travelPlanApi.getTravelPlansByUser(42L) } returns PagedResponseTravelPlanDto(
            status = 200,
            message = "OK",
            data = listOf(dto),
        )
        val r = repo.getUserTrips("42")
        assertTrue(r.isSuccess)
        coVerify { tripDao.deleteAllTrips() }
        coVerify { tripDao.insertTrips(any()) }
    }

    @Test
    fun `getTripById remote path`() = runTest {
        every { preferencesManager.getAuthToken() } returns "t"
        every { preferencesManager.getCurrentUserId() } returns "42"
        val dto = TestFixtures.travelPlanDto(id = 3L)
        coEvery { travelPlanApi.getTravelPlanById(3L) } returns apiOk(dto)
        val r = repo.getTripById("3")
        assertTrue(r.isSuccess)
        coVerify { tripDao.insertTrip(any()) }
    }

    @Test
    fun `createTrip requires auth`() = runTest {
        every { preferencesManager.getAuthToken() } returns null
        val trip = tripMapper.fromTravelPlanDto(TestFixtures.travelPlanDto(), "42")
        assertTrue(repo.createTrip(trip).isFailure)
    }

    @Test
    fun `createTrip success`() = runTest {
        every { preferencesManager.getAuthToken() } returns "t"
        every { preferencesManager.getCurrentUserId() } returns "42"
        val created = TestFixtures.travelPlanDto(id = 100L)
        coEvery { travelPlanApi.createTravelPlan(any()) } returns apiOk(created, 201)
        val trip = tripMapper.fromTravelPlanDto(TestFixtures.travelPlanDto(id = null), "42")
        assertTrue(repo.createTrip(trip).isSuccess)
    }

    @Test
    fun `updateTrip and deleteTrip`() = runTest {
        every { preferencesManager.getAuthToken() } returns "t"
        every { preferencesManager.getCurrentUserId() } returns "42"
        val updated = TestFixtures.travelPlanDto(id = 5L)
        val trip = tripMapper.fromTravelPlanDto(updated, "42")
        coEvery { travelPlanApi.updateTravelPlan(5L, any()) } returns apiOk(updated)
        assertTrue(repo.updateTrip(trip).isSuccess)

        coEvery { travelPlanApi.deleteTravelPlan(5L) } returns apiOk(null)
        assertTrue(repo.deleteTrip("5").isSuccess)
        coVerify { tripDao.deleteTripById("5") }
    }

    @Test
    fun `filter trips by status from my plans`() = runTest {
        every { preferencesManager.getAuthToken() } returns "t"
        val active = TestFixtures.travelPlanDto().copy(status = TravelPlanStatus.ACTIVE, id = 1L)
        val draft = TestFixtures.travelPlanDto().copy(status = TravelPlanStatus.DRAFT, id = 2L)
        coEvery { travelPlanApi.getMyTravelPlans() } returns apiOk(listOf(active, draft))
        val r = repo.getActiveTrips("42")
        assertTrue(r.isSuccess)
        assertTrue(r.getOrThrow().all { it.status == TripStatus.ACTIVE })

        val completed = TestFixtures.travelPlanDto().copy(status = TravelPlanStatus.COMPLETED, id = 3L)
        coEvery { travelPlanApi.getMyTravelPlans() } returns apiOk(listOf(completed))
        assertEquals(1, repo.getCompletedTrips("42").getOrThrow().size)

        val upcoming = TestFixtures.travelPlanDto().copy(
            id = 4L,
            status = TravelPlanStatus.ACTIVE,
            startDate = "2030-06-01T00:00:00Z",
            endDate = "2030-06-10T00:00:00Z",
        )
        coEvery { travelPlanApi.getMyTravelPlans() } returns apiOk(listOf(upcoming))
        val up = repo.getUpcomingTrips("42")
        assertTrue(up.isSuccess)
        assertEquals(1, up.getOrThrow().size)

        val lima = TestFixtures.travelPlanDto().copy(destinationLocation = "Lima Peru", id = 5L)
        coEvery { travelPlanApi.getMyTravelPlans() } returns apiOk(listOf(lima))
        val search = repo.searchTripsByDestination("42", "lima")
        assertEquals(1, search.getOrThrow().size)
    }

    @Test
    fun `streamTripUpdates maps dao flow`() = runTest {
        coEvery { tripDao.streamTripById("9") } returns flowOf(entity("9"))
        val v = repo.streamTripUpdates("9").first()
        assertEquals("9", v!!.id)
    }
}
