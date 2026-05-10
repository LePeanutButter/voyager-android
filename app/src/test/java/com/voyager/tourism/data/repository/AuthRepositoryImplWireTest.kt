package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.ApiResponseEnvelope
import com.voyager.tourism.data.api.GoogleAuthApiService
import com.voyager.tourism.data.api.RetrofitMockWebServerRule
import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.dto.LoginResponseDto
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.util.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * End-to-end parsing for [AuthRepositoryImpl] with real Retrofit/Moshi and mocked HTTP.
 */
class AuthRepositoryImplWireTest {

    @get:Rule
    val serverRule = RetrofitMockWebServerRule()

    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        val retrofit = serverRule.retrofit
        repository = AuthRepositoryImpl(
            retrofit.create(UserApiService::class.java),
            retrofit.create(GoogleAuthApiService::class.java),
        )
    }

    @Test
    fun `registerUser maps 201 ApiResponse to success`() = runTest {
        val user = TestFixtures.userDto(id = 200L)
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.success(
                        serverRule.moshi,
                        user,
                        UserDto::class.java,
                        status = 201,
                        message = "Registered",
                    ),
                ),
        )

        val result = repository.registerUser("u", "e@e.com", "pw", "F", "L")

        assertTrue(result.isSuccess)
        assertEquals(200L, result.getOrThrow().id)
    }

    @Test
    fun `loginUser maps 200 envelope to success`() = runTest {
        val user = TestFixtures.userDto(token = null)
        val login = TestFixtures.loginResponseDto(user = user, token = "jwt-abc")
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, login, LoginResponseDto::class.java)),
        )

        val result = repository.loginUser("traveler@mail.com", "secret")

        assertTrue(result.isSuccess)
        assertEquals(user.email, result.getOrThrow().email)
        assertEquals("jwt-abc", result.getOrThrow().token)
    }

    @Test
    fun `loginUser failure when data null`() = runTest {
        val json = ApiResponseEnvelope.success(serverRule.moshi, null, LoginResponseDto::class.java, status = 401, message = "Bad creds")
        serverRule.server.enqueue(MockResponse().setBody(json))

        val result = repository.loginUser("x", "y")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Bad creds") == true)
    }

    @Test
    fun `initiateGoogleLogin returns url string`() = runTest {
        serverRule.server.enqueue(
            MockResponse()
                .setBody(
                    ApiResponseEnvelope.success(
                        serverRule.moshi,
                        "https://oauth.example/authorize",
                        String::class.java,
                    ),
                ),
        )

        val result = repository.initiateGoogleLogin()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().startsWith("https://"))
    }

    @Test
    fun `exchangeGoogleCode maps user envelope`() = runTest {
        val user = TestFixtures.userDto()
        serverRule.server.enqueue(
            MockResponse()
                .setBody(ApiResponseEnvelope.success(serverRule.moshi, user, UserDto::class.java)),
        )

        val result = repository.exchangeGoogleCode("code", "state")

        assertTrue(result.isSuccess)
        assertEquals(user.id, result.getOrThrow().id)
    }
}
