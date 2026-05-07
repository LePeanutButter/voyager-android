package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.util.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SocialApiServiceWireTest {

    @get:Rule
    val serverRule = RetrofitMockWebServerRule()

    private lateinit var api: SocialApiService

    @Before
    fun setup() {
        api = serverRule.retrofit.create(SocialApiService::class.java)
    }

    @Test
    fun `sendConnectionRequest POST body matches SendConnectionRequestDto json names`() = runTest {
        val created = TestFixtures.connectionRequest(id = 50L)
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, created, ConnectionRequestDto::class.java)),
        )

        val r = api.sendConnectionRequest(
            SendConnectionRequestDto(recipientId = 88L, message = "Let's meet!"),
        )

        assertEquals(200, r.status)
        assertEquals(50L, r.data?.id)

        val req = serverRule.server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.path!!.endsWith("/api/v1/social/connections"))
        val raw = req.bodyUtf8()
        assertTrue(raw.contains("\"recipientId\":88"))
        assertTrue(raw.contains("\"message\":\"Let's meet!\""))
    }

    @Test
    fun `getPendingRequests GET returns list envelope`() = runTest {
        val pending = listOf(TestFixtures.connectionRequest())
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.successList(
                        serverRule.moshi,
                        pending,
                        ConnectionRequestDto::class.java,
                    ),
                ),
        )

        val r = api.getPendingRequests()

        assertEquals(200, r.status)
        assertEquals(1, r.data?.size)

        val req = serverRule.server.takeRequest()
        assertEquals("GET", req.method)
        assertTrue(req.path!!.endsWith("/api/v1/social/connections/pending"))
    }

    @Test
    fun `acceptConnectionRequest PUT uses path requestId`() = runTest {
        val updated = TestFixtures.connectionRequest(status = "ACCEPTED")
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, updated, ConnectionRequestDto::class.java)),
        )

        val r = api.acceptConnectionRequest(42L)

        assertEquals(200, r.status)
        assertEquals("ACCEPTED", r.data?.status)

        val req = serverRule.server.takeRequest()
        assertEquals("PUT", req.method)
        assertTrue(req.path!!.contains("/social/connections/42/accept"))
    }

    private fun okhttp3.mockwebserver.RecordedRequest.bodyUtf8(): String =
        body!!.clone().readUtf8()
}
