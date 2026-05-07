package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.ApiResponseEnvelope
import com.voyager.tourism.data.api.BackendMiscApiService
import com.voyager.tourism.data.api.RetrofitMockWebServerRule
import com.voyager.tourism.data.api.SocialApiService
import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.dto.CompatibilityMatchResponseDto
import com.voyager.tourism.data.dto.ConnectionDto
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.SharedActivityResponseDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.data.dto.TravelPlanActivityDto
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.util.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SocialRepositoryImplWireTest {

    @get:Rule
    val serverRule = RetrofitMockWebServerRule()

    private lateinit var repository: SocialRepositoryImpl

    @Before
    fun setup() {
        val retrofit = serverRule.retrofit
        repository = SocialRepositoryImpl(
            retrofit.create(SocialApiService::class.java),
            retrofit.create(TravelPlanApiService::class.java),
            retrofit.create(BackendMiscApiService::class.java),
        )
    }

    @Test
    fun `getCompatibleTravelers parses travel plan wire response`() = runTest {
        val match = TestFixtures.travelerMatch()
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.successList(
                        serverRule.moshi,
                        listOf(match),
                        TravelerMatchDto::class.java,
                    ),
                ),
        )

        val list = repository.getCompatibleTravelers("42", "ignored-token")

        assertEquals(1, list.size)
        assertEquals(match.userId, list.first().userId)
    }

    @Test
    fun `sendConnectionRequest returns dto from social connections POST`() = runTest {
        val created = TestFixtures.connectionRequest(id = 33L)
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, created, ConnectionRequestDto::class.java)),
        )

        val dto = repository.sendConnectionRequest(
            SendConnectionRequestDto(recipientId = 1L, message = "Hi"),
            "token",
        )

        assertEquals(33L, dto.id)
    }

    @Test
    fun `getCompatibilityMatches posts structured request and maps scores`() = runTest {
        val row = CompatibilityMatchResponseDto(
            userId = 1L,
            totalScore = 1.0,
            destinationScore = 0.5,
            dateProximityScore = 0.5,
            interestScore = 0.5,
            matchedInterests = listOf("x"),
        )
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.successList(
                        serverRule.moshi,
                        listOf(row),
                        CompatibilityMatchResponseDto::class.java,
                    ),
                ),
        )

        val matches = repository.getCompatibilityMatches(
            destination = "Cusco",
            startDate = "2027-01-01",
            endDate = "2027-01-20",
            interests = listOf("hiking"),
        )

        assertTrue(matches.isSuccess)
        assertEquals(1L, matches.getOrThrow().first().userId)
        assertEquals(1.0, matches.getOrThrow().first().totalScore, 0.001)
    }

    @Test
    fun `getTravelPlanActivities maps activity list`() = runTest {
        val activity = TravelPlanActivityDto(
            id = 9L,
            name = "City tour",
            description = null,
            type = null,
            startTime = null,
            endTime = null,
            location = null,
            estimatedCost = null,
            actualCost = null,
            bookingReference = null,
            isConfirmed = null,
            notes = null,
            createdAt = null,
            updatedAt = null,
        )
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.successList(
                        serverRule.moshi,
                        listOf(activity),
                        TravelPlanActivityDto::class.java,
                    ),
                ),
        )

        val r = repository.getTravelPlanActivities(5L)

        assertTrue(r.isSuccess)
        assertEquals(9L, r.getOrThrow().first().id)
        assertEquals("City tour", r.getOrThrow().first().name)
    }

    @Test
    fun `getConnections maps to domain`() = runTest {
        val row = ConnectionDto(userId = 8L, username = "pal", firstName = null, lastName = null, status = "ACTIVE")
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.successList(
                        serverRule.moshi,
                        listOf(row),
                        ConnectionDto::class.java,
                    ),
                ),
        )
        val r = repository.getConnections(1L)
        assertTrue(r.isSuccess)
        assertEquals(8L, r.getOrThrow().first().id)
        assertEquals("pal", r.getOrThrow().first().username)
    }

    @Test
    fun `shareActivity and resolveSharedActivity use misc api`() = runTest {
        val shared = SharedActivityResponseDto(
            id = 1L,
            activityId = 2L,
            senderId = 3L,
            receiverId = 4L,
            status = "PENDING",
            sharedPlan = false,
        )
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, shared, SharedActivityResponseDto::class.java)),
        )
        val shareResult = repository.shareActivity(10L, 99L)
        assertTrue(shareResult.isSuccess)
        assertEquals(1L, shareResult.getOrThrow().id)

        val updated = shared.copy(status = "ACCEPTED")
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, updated, SharedActivityResponseDto::class.java)),
        )
        val resolve = repository.resolveSharedActivity(1L, com.voyager.tourism.domain.model.SharedActivityDecision.ACCEPT)
        assertTrue(resolve.isSuccess)
        assertEquals("ACCEPTED", resolve.getOrThrow().status)
    }
}
