package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.GoogleAuthApiService
import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.dto.GoogleServerAuthRequest
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.LoginResponseDto
import com.voyager.tourism.data.dto.UserLoginDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.util.TestFixtures
import com.voyager.tourism.util.TestFixtures.apiResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest {

    private val userApiService = mockk<UserApiService>()
    private val googleAuthApiService = mockk<GoogleAuthApiService>()
    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setup() {
        authRepository = AuthRepositoryImpl(userApiService, googleAuthApiService)
    }

    @Test
    fun `registerUser success returns data`() = runBlocking {
        val request = UserRegistrationDto(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User",
        )
        val userDto = TestFixtures.userDto(id = 123L)
        coEvery { userApiService.registerUser(request) } returns apiResponse(200, userDto)

        val result = authRepository.registerUser(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
        )

        assertTrue(result.isSuccess)
        assertEquals(userDto, result.getOrNull())
    }

    @Test
    fun `registerUser status 201 returns success`() = runBlocking {
        val request = UserRegistrationDto(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User",
        )
        val userDto = TestFixtures.userDto(id = 124L)
        coEvery { userApiService.registerUser(request) } returns apiResponse(201, userDto)

        val result = authRepository.registerUser(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
        )

        assertTrue(result.isSuccess)
        assertEquals(userDto, result.getOrNull())
    }

    @Test
    fun `registerUser with null data returns failure`() = runBlocking {
        val request = UserRegistrationDto(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User",
        )
        coEvery { userApiService.registerUser(request) } returns apiResponse<UserDto>(200, null, message = "")

        val result = authRepository.registerUser(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
        )

        assertTrue(result.isFailure)
        assertEquals("Registration failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerUser with error status returns failure`() = runBlocking {
        val request = UserRegistrationDto(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User",
        )
        coEvery { userApiService.registerUser(request) } returns apiResponse<UserDto>(
            status = 400,
            data = null,
            message = "Bad request",
        )

        val result = authRepository.registerUser(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
        )

        assertTrue(result.isFailure)
        assertEquals("Bad request", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerUser with network exception returns failure`() = runBlocking {
        coEvery { userApiService.registerUser(any()) } throws RuntimeException("Network error")

        val result = authRepository.registerUser(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User",
        )

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginUser success merges token into user`() = runBlocking {
        val request = UserLoginDto(
            usernameOrEmail = "test@example.com",
            password = "password123",
        )
        val userCore = TestFixtures.userDto(id = 123L, token = null)
        val loginPayload = TestFixtures.loginResponseDto(user = userCore, token = "jwt-token")
        coEvery { userApiService.loginUser(request) } returns apiResponse(200, loginPayload)

        val result = authRepository.loginUser("test@example.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals("jwt-token", result.getOrNull()?.token)
        assertEquals(123L, result.getOrNull()?.id)
    }

    @Test
    fun `loginUser with null data returns failure`() = runBlocking {
        val request = UserLoginDto(
            usernameOrEmail = "test@example.com",
            password = "password123",
        )
        coEvery { userApiService.loginUser(request) } returns apiResponse<LoginResponseDto>(200, null, message = "")

        val result = authRepository.loginUser("test@example.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Login failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginUser with error status returns failure`() = runBlocking {
        val request = UserLoginDto(
            usernameOrEmail = "test@example.com",
            password = "password123",
        )
        coEvery { userApiService.loginUser(request) } returns apiResponse(
            401,
            null,
            message = "Unauthorized",
        )

        val result = authRepository.loginUser("test@example.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginUser with network exception returns failure`() = runBlocking {
        coEvery { userApiService.loginUser(any()) } throws RuntimeException("Network timeout")

        val result = authRepository.loginUser("test@example.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Network timeout", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exchangeGoogleCode success`() = runBlocking {
        val body = GoogleServerAuthRequest(code = "auth-code")
        val userDto = TestFixtures.userDto(id = 55L)
        coEvery {
            googleAuthApiService.exchangeGoogleServerAuthCode(body)
        } returns apiResponse(200, userDto)

        val result = authRepository.exchangeGoogleCode("auth-code", "state")

        assertTrue(result.isSuccess)
        assertEquals(userDto, result.getOrNull())
    }

    @Test
    fun `exchangeGoogleCode with null data returns failure`() = runBlocking {
        val body = GoogleServerAuthRequest(code = "auth-code")
        coEvery {
            googleAuthApiService.exchangeGoogleServerAuthCode(body)
        } returns apiResponse<UserDto>(200, null, message = "")

        val result = authRepository.exchangeGoogleCode("auth-code", "state")

        assertTrue(result.isFailure)
        assertEquals("Google OAuth2 failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exchangeGoogleCode with error status returns failure`() = runBlocking {
        val body = GoogleServerAuthRequest(code = "invalid-code")
        coEvery {
            googleAuthApiService.exchangeGoogleServerAuthCode(body)
        } returns apiResponse<UserDto>(400, null, message = "Invalid code")

        val result = authRepository.exchangeGoogleCode("invalid-code", "state")

        assertTrue(result.isFailure)
        assertEquals("Invalid code", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exchangeGoogleCode with network exception returns failure`() = runBlocking {
        coEvery { googleAuthApiService.exchangeGoogleServerAuthCode(any()) } throws RuntimeException("Network error")

        val result = authRepository.exchangeGoogleCode("auth-code", "state")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    private fun googleLoginResponse(code: Int, location: String?): Response<Unit> {
        val r = mockk<Response<Unit>>()
        every { r.code() } returns code
        every { r.headers() } returns if (location != null) {
            Headers.Builder().add("Location", location).build()
        } else {
            Headers.Builder().build()
        }
        return r
    }

    @Test
    fun `initiateGoogleLogin 302 with Location returns url`() = runBlocking {
        val url = "https://accounts.google.com/oauth/authorize"
        coEvery { googleAuthApiService.initiateGoogleLogin() } returns googleLoginResponse(302, url)

        val result = authRepository.initiateGoogleLogin()

        assertTrue(result.isSuccess)
        assertEquals(url, result.getOrNull())
    }

    @Test
    fun `initiateGoogleLogin 301 with Location returns url`() = runBlocking {
        val url = "https://accounts.google.com/oauth/authorize"
        coEvery { googleAuthApiService.initiateGoogleLogin() } returns googleLoginResponse(301, url)

        val result = authRepository.initiateGoogleLogin()

        assertTrue(result.isSuccess)
        assertEquals(url, result.getOrNull())
    }

    @Test
    fun `initiateGoogleLogin 200 with Location returns url`() = runBlocking {
        val url = "https://accounts.google.com/oauth/authorize"
        coEvery { googleAuthApiService.initiateGoogleLogin() } returns googleLoginResponse(200, url)

        val result = authRepository.initiateGoogleLogin()

        assertTrue(result.isSuccess)
        assertEquals(url, result.getOrNull())
    }

    @Test
    fun `initiateGoogleLogin missing location returns failure`() = runBlocking {
        coEvery { googleAuthApiService.initiateGoogleLogin() } returns googleLoginResponse(200, null)

        val result = authRepository.initiateGoogleLogin()

        assertTrue(result.isFailure)
        assertEquals(
            "No se pudo obtener la URL de redirección (Status: 200)",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun `initiateGoogleLogin with network exception returns failure`() = runBlocking {
        coEvery { googleAuthApiService.initiateGoogleLogin() } throws RuntimeException("Network error")

        val result = authRepository.initiateGoogleLogin()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `multiple register then login calls`() = runBlocking {
        val r1 = UserRegistrationDto("u1", "e1@e.com", "p", "F", "L")
        val r2 = UserRegistrationDto("u2", "e2@e.com", "p2", "F2", "L2")
        val u1 = TestFixtures.userDto(id = 1L)
        val u2 = TestFixtures.userDto(id = 2L)
        coEvery { userApiService.registerUser(r1) } returns apiResponse(201, u1)
        coEvery { userApiService.registerUser(r2) } returns apiResponse(200, u2)

        val res1 = authRepository.registerUser("u1", "e1@e.com", "p", "F", "L")
        val res2 = authRepository.registerUser("u2", "e2@e.com", "p2", "F2", "L2")

        assertTrue(res1.isSuccess && res2.isSuccess)
        assertEquals(u1, res1.getOrNull())
        assertEquals(u2, res2.getOrNull())

        val loginReq = UserLoginDto("e1@e.com", "p")
        val lp = TestFixtures.loginResponseDto(user = u1, token = "t1")
        coEvery { userApiService.loginUser(loginReq) } returns apiResponse(200, lp)

        val lr = authRepository.loginUser("e1@e.com", "p")
        assertTrue(lr.isSuccess)
        assertEquals("t1", lr.getOrNull()?.token)
    }
}
