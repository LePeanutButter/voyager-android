package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.GoogleServerAuthRequest
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
    fun `initiateGoogleLogin GET returns 302 redirect with Location header`() = runTest {
        val googleUrl = "https://accounts.google.com/o/oauth2/v2/auth?client=1"
        serverRule.server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Location", googleUrl)
        )

        val response = api.initiateGoogleLogin()

        assertEquals(200, response.code())
        assertEquals(googleUrl, response.headers()["Location"])

        val req = serverRule.server.takeRequest()
        assertEquals("GET", req.method)
        assertTrue(req.path!!.contains("/api/v1/auth/google/login"))
    }

    @Test
    fun `exchangeGoogleServerAuthCode POST sends JSON body`() = runTest {
        val user = TestFixtures.userDto()
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, user, UserDto::class.java)),
        )

        val r = api.exchangeGoogleServerAuthCode(GoogleServerAuthRequest(code = "auth-code-xyz"))

        assertEquals(200, r.status)
        assertEquals(user.id, r.data?.id)

        val req = serverRule.server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.path!!.contains("/api/v1/auth/google/token"))
        assertTrue(req.body.readUtf8().contains("auth-code-xyz"))
    }
}
