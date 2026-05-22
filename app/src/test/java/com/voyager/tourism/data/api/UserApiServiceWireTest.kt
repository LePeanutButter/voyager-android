package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.LoginResponseDto
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserLoginDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.util.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Retrofit + Moshi against [MockWebServer]: verifies request JSON field names match backend DTOs
 * and [ApiResponse] envelopes parse correctly.
 */
class UserApiServiceWireTest {

    @get:Rule
    val serverRule = RetrofitMockWebServerRule()

    private lateinit var api: UserApiService

    @Before
    fun setup() {
        api = serverRule.retrofit.create(UserApiService::class.java)
    }

    @Test
    fun `login serializes UserLoginDto and parses ApiResponse UserDto`() = runTest {
        val user = TestFixtures.userDto(token = "jwt-token")
        serverRule.server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, user, UserDto::class.java)),
        )

        val response = api.loginUser(UserLoginDto(usernameOrEmail = "traveler@mail.com", password = "p4ss"))

        assertEquals(200, response.status)
        assertEquals(user.id, response.data?.id)
        assertEquals(user.email, response.data?.email)
        assertEquals("jwt-token", response.data?.token)

        val req = serverRule.server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.path!!.endsWith("/api/v1/users/login"))
        val raw = req.bodyBufferUtf8()
        assertTrue(raw.contains("\"usernameOrEmail\":\"traveler@mail.com\""))
        assertTrue(raw.contains("\"password\":\"p4ss\""))
    }

    @Test
    fun `register serializes UserRegistrationDto with backend json names`() = runTest {
        val user = TestFixtures.userDto(id = 77L)
        serverRule.server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    ApiResponseEnvelope.success(
                        moshi = serverRule.moshi,
                        data = user,
                        dataClass = UserDto::class.java,
                        status = 201,
                        message = "Created",
                        path = "/api/v1/users",
                    ),
                ),
        )

        val response = api.registerUser(
            UserRegistrationDto(
                username = "newbie",
                email = "n@example.com",
                password = "secret",
                firstName = "N",
                lastName = "W",
                phoneNumber = null,
            ),
        )

        assertEquals(201, response.status)
        assertEquals(77L, response.data?.id)

        val req = serverRule.server.takeRequest()
        assertTrue(req.path!!.endsWith("/api/v1/users"))
        val raw = req.bodyBufferUtf8()
        assertTrue(raw.contains("\"username\":\"newbie\""))
        assertTrue(raw.contains("\"email\":\"n@example.com\""))
        assertTrue(raw.contains("\"password\":\"secret\""))
        assertTrue(raw.contains("\"firstName\":\"N\""))
        assertTrue(raw.contains("\"lastName\":\"W\""))
    }

    private fun okhttp3.mockwebserver.RecordedRequest.bodyBufferUtf8(): String =
        body!!.clone().readUtf8()
}
