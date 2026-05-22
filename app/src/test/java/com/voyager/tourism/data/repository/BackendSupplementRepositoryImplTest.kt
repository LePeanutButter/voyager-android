package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.BackendMiscApiService
import com.voyager.tourism.data.api.SocialApiService
import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.MatchResponseDto
import com.voyager.tourism.data.dto.PagedResponseSocialReviewDto
import com.voyager.tourism.data.dto.PagedResponseTravelPlanDto
import com.voyager.tourism.data.dto.PagedResponseUserDto
import com.voyager.tourism.data.dto.ShareActivityRequestDto
import com.voyager.tourism.data.dto.SharedActivityActionRequestDto
import com.voyager.tourism.data.dto.SharedActivityResponseDto
import com.voyager.tourism.data.dto.SocialReviewDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.data.dto.UserStatisticsDto
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class BackendSupplementRepositoryImplTest {

    private val userApi = mockk<UserApiService>()
    private val travelPlanApi = mockk<TravelPlanApiService>()
    private val socialApi = mockk<SocialApiService>()
    private val miscApi = mockk<BackendMiscApiService>()
    private lateinit var repo: BackendSupplementRepositoryImpl

    private fun <T> ok(data: T?) = ApiResponse("t", 200, "OK", data, null, null)

    @Before
    fun setup() {
        repo = BackendSupplementRepositoryImpl(userApi, travelPlanApi, socialApi, miscApi)
    }

    @Test
    fun `forwards user supplement calls`() = runTest {
        val u = TestFixtures.userDto()
        val reg = UserRegistrationDto("a", "b@c.com", "p", "F", "L")
        coEvery { userApi.registerUserAlias(reg) } returns ok(u)
        assertEquals(u, repo.registerUserAlias(reg).data)

        coEvery { userApi.checkUsernameAvailability("x") } returns ok(true)
        assertEquals(true, repo.checkUsernameAvailability("x").data)

        coEvery { userApi.getUserStatistics() } returns ok(UserStatisticsDto(1, 1))
        assertEquals(1L, repo.getUserStatistics().data?.totalUsers)

        val paged = PagedResponseUserDto(status = 200, message = "OK", data = emptyList())
        coEvery { userApi.getAllUsers(0, 10, "id", "asc") } returns paged
        assertEquals(paged, repo.getAllUsers(0, 10, "id", "asc"))
        coVerify { userApi.getAllUsers(0, 10, "id", "asc") }
    }

    @Test
    fun `forwards travel plan and social calls`() = runTest {
        coEvery { travelPlanApi.shareTravelPlan(3L) } returns ok("token")
        assertEquals("token", repo.shareTravelPlan(3L).data)

        val plans = PagedResponseTravelPlanDto(status = 200, message = "OK", data = emptyList())
        coEvery { travelPlanApi.getTravelPlansByStatus("ACTIVE", 0, 20) } returns plans
        assertEquals(plans, repo.getTravelPlansByStatus("ACTIVE", 0, 20))

        coEvery { socialApi.removeConnection(9L) } returns ok(null)
        repo.removeConnection(9L)
        coVerify { socialApi.removeConnection(9L) }

        val reviewPaged = PagedResponseSocialReviewDto(emptyList(), 0, 10, 0, 0, true)
        coEvery { socialApi.getReviews("HOTEL", 1L, 0, 10) } returns ok(reviewPaged)
        assertEquals(200, repo.getReviews("HOTEL", 1L, 0, 10).status)
    }

    @Test
    fun `forwards misc destination and legacy shared activity`() = runTest {
        val matches = listOf(MatchResponseDto(userId = 1L))
        coEvery {
            miscApi.getDestinationMatches("Paris", "2027-01-01", "2027-02-01", listOf("art"), 10)
        } returns ok(matches)
        assertEquals(1, repo.getDestinationMatches("Paris", "2027-01-01", "2027-02-01", listOf("art"), 10).data!!.size)

        val shared = SharedActivityResponseDto(1L, 2L, 3L, 4L, "PENDING", false)
        coEvery { miscApi.legacyShareActivity(5L, ShareActivityRequestDto(6L)) } returns ok(shared)
        assertEquals(
            shared,
            repo.legacyShareActivity(5L, ShareActivityRequestDto(6L)).data,
        )
        coEvery {
            miscApi.legacyUpdateSharedActivity(7L, SharedActivityActionRequestDto("ACCEPT"))
        } returns ok(shared.copy(status = "ACCEPTED"))
        assertEquals(
            "ACCEPTED",
            repo.legacyUpdateSharedActivity(7L, SharedActivityActionRequestDto("ACCEPT")).data?.status,
        )
    }
}
