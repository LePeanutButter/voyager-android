package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.CompatibilityMatchRequestDto
import com.voyager.tourism.data.dto.CompatibilityMatchResponseDto
import com.voyager.tourism.data.dto.SharedActivityActionRequestDto
import com.voyager.tourism.data.dto.SharedActivityResponseDto
import com.voyager.tourism.data.dto.ShareActivityRequestDto
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BackendMiscApiServiceWireTest {

    @get:Rule
    val serverRule = RetrofitMockWebServerRule()

    private lateinit var api: BackendMiscApiService

    @Before
    fun setup() {
        api = serverRule.retrofit.create(BackendMiscApiService::class.java)
    }

    @Test
    fun `findCompatibilityMatches POST CompatibilityMatchRequestDto shape`() = runTest {
        val row = CompatibilityMatchResponseDto(
            userId = 9L,
            totalScore = 0.9,
            destinationScore = 0.8,
            dateProximityScore = 0.7,
            interestScore = 0.6,
            matchedInterests = listOf("museums"),
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

        val r = api.findCompatibilityMatches(
            CompatibilityMatchRequestDto(
                destination = "Lima",
                startDate = "2027-06-01",
                endDate = "2027-06-15",
                interests = listOf("museums", "food"),
            ),
        )

        assertEquals(200, r.status)
        assertEquals(9L, r.data!!.first().userId)

        val req = serverRule.server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.path!!.endsWith("/api/v1/compatibility/matches"))
        val raw = req.bodyUtf8()
        assertTrue(raw.contains("\"destination\":\"Lima\""))
        assertTrue(raw.contains("\"startDate\":\"2027-06-01\""))
        assertTrue(raw.contains("\"endDate\":\"2027-06-15\""))
        assertTrue(raw.contains("\"interests\""))
        assertTrue(raw.contains("museums"))
        assertTrue(raw.contains("food"))
    }

    @Test
    fun `shareActivity POST ShareActivityRequestDto with path activityId`() = runTest {
        val dto = SharedActivityResponseDto(
            id = 1L,
            activityId = 2L,
            senderId = 3L,
            receiverId = 4L,
            status = "PENDING",
            sharedPlan = false,
        )
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, dto, SharedActivityResponseDto::class.java)),
        )

        val r = api.shareActivity(99L, ShareActivityRequestDto(receiverId = 100L))

        assertEquals(200, r.status)
        assertEquals(1L, r.data?.id)

        val req = serverRule.server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.path!!.contains("/api/v1/activities/99/share"))
        val raw = req.bodyUtf8()
        assertTrue(raw.contains("\"receiverId\":100"))
    }

    @Test
    fun `updateSharedActivity PATCH SharedActivityActionRequestDto`() = runTest {
        val dto = SharedActivityResponseDto(
            id = 5L,
            activityId = 6L,
            senderId = 7L,
            receiverId = 8L,
            status = "ACCEPTED",
            sharedPlan = true,
        )
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, dto, SharedActivityResponseDto::class.java)),
        )

        val r = api.updateSharedActivity(55L, SharedActivityActionRequestDto(action = "ACCEPT"))

        assertEquals("ACCEPTED", r.data?.status)

        val req = serverRule.server.takeRequest()
        assertEquals("PATCH", req.method)
        assertTrue(req.path!!.endsWith("/api/v1/shared-activities/55"))
        val raw = req.bodyUtf8()
        assertTrue(raw.contains("\"action\":\"ACCEPT\""))
    }

    private fun okhttp3.mockwebserver.RecordedRequest.bodyUtf8(): String =
        body!!.clone().readUtf8()
}
