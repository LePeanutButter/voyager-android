package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.TravelApiService
import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.PagedResponseTravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanRequest
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TravelRepositoryImplTest {

    private val travelApi = mockk<TravelApiService>()
    private lateinit var repo: TravelRepositoryImpl

    @Before
    fun setup() {
        repo = TravelRepositoryImpl(travelApi)
    }

    private fun <T> apiOk(data: T?, status: Int = 200, msg: String = "OK") =
        ApiResponse("t", status, msg, data, null, null)

    @Test
    fun `createTravelPlan success`() = runTest {
        val created = TestFixtures.travelPlanDto()
        coEvery { travelApi.createTravelPlan(any()) } returns apiOk(created, 201)
        val r = repo.createTravelPlan(
            TravelPlanRequest(
                title = "T",
                destinationLocation = "B",
                originLocation = "A",
                startDate = "2027-01-01",
                endDate = "2027-01-10",
                estimatedBudget = 100.0,
                numberOfTravelers = 2,
                description = "D",
            ),
        )
        assertTrue(r.isSuccess)
    }

    @Test
    fun `getUserTravelPlans invalid user fails`() = runTest {
        val r = repo.getUserTravelPlans("x")
        assertTrue(r.isFailure)
    }

    @Test
    fun `getUserTravelPlans success`() = runTest {
        coEvery { travelApi.getUserTravelPlans(1L) } returns PagedResponseTravelPlanDto(
            status = 200,
            message = "OK",
            data = listOf(TestFixtures.travelPlanDto()),
        )
        val r = repo.getUserTravelPlans("1")
        assertTrue(r.isSuccess)
        assertEquals(1, r.getOrThrow().size)
    }

    @Test
    fun `getTravelPlanById invalid fails`() = runTest {
        assertTrue(repo.getTravelPlanById("abc").isFailure)
    }

    @Test
    fun `getTravelPlanById success`() = runTest {
        val p = TestFixtures.travelPlanDto()
        coEvery { travelApi.getTravelPlanById(5L) } returns apiOk(p)
        assertTrue(repo.getTravelPlanById("5").isSuccess)
    }

    @Test
    fun `update and delete`() = runTest {
        val p = TestFixtures.travelPlanDto()
        coEvery { travelApi.updateTravelPlan(1L, any()) } returns apiOk(p)
        val req = TravelPlanRequest(
            title = "t",
            destinationLocation = "X",
            originLocation = null,
            startDate = "2027-02-01",
            endDate = "2027-02-05",
            estimatedBudget = 1.0,
            numberOfTravelers = 1,
            description = "d",
        )
        assertTrue(repo.updateTravelPlan("1", req).isSuccess)

        coEvery { travelApi.deleteTravelPlan(2L) } returns apiOk(null)
        assertTrue(repo.deleteTravelPlan("2").isSuccess)
    }
}
