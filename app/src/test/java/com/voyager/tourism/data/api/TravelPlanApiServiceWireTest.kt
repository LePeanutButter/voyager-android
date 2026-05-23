package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.util.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TravelPlanApiServiceWireTest {

    @get:Rule
    val serverRule = RetrofitMockWebServerRule()

    private lateinit var api: TravelPlanApiService

    @Before
    fun setup() {
        api = serverRule.retrofit.create(TravelPlanApiService::class.java)
    }

    @Test
    fun `findCompatibleTravelers GET uses plan id in path`() = runTest {
        val matches = listOf(TestFixtures.travelerMatch(userId = 3L))
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.successList(
                        serverRule.moshi,
                        matches,
                        TravelerMatchDto::class.java,
                    ),
                ),
        )

        val r = api.findCompatibleTravelers(planId = 15L)

        assertEquals(200, r.status)
        assertEquals(3L, r.data!!.first().userId)

        val req = serverRule.server.takeRequest()
        assertEquals("GET", req.method)
        assertTrue(req.path!!.endsWith("/api/v1/travel-plans/15/compatible-travelers"))
    }
}
