package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.util.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GoogleAuthApiServiceWireTest {

    @get:Rule
    val serverRule = RetrofitMockWebServerRule()

    private lateinit var api: GoogleAuthApiService

    @Before
    fun setup() {
        api = serverRule.retrofit.create(GoogleAuthApiService::class.java)
    }

    @Test
    fun `initiateGoogleLogin GET returns ApiResponse string data`() = runTest {
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.success(
                        serverRule.moshi,
                        "https://accounts.google.com/o/oauth2/v2/auth?client=1",
                        String::class.java,
                    ),
                ),
        )

        val r = api.initiateGoogleLogin()

        assertEquals(200, r.status)
        assertTrue(r.data!!.contains("google.com"))

        val req = serverRule.server.takeRequest()
        assertEquals("GET", req.method)
        assertTrue(req.path!!.contains("/api/v1/auth/google/login"))
    }

    @Test
    fun `handleGoogleCallback GET uses code and state query params`() = runTest {
        val user = TestFixtures.userDto()
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, user, UserDto::class.java)),
        )

        val r = api.handleGoogleCallback(code = "auth-code-xyz", state = "csrf-state")

        assertEquals(200, r.status)
        assertEquals(user.id, r.data?.id)

        val req = serverRule.server.takeRequest()
        assertEquals("GET", req.method)
        val path = req.requestUrl!!.encodedPath
        assertTrue(path.contains("auth/google/callback"))
        assertEquals("auth-code-xyz", req.requestUrl!!.queryParameter("code"))
        assertEquals("csrf-state", req.requestUrl!!.queryParameter("state"))
    }
}
