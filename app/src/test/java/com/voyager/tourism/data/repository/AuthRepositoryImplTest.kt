package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.GoogleAuthApiService
import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.dto.GoogleServerAuthRequest
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserLoginDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import okhttp3.Headers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val userApiService = mockk<UserApiService>()
    private val googleAuthApiService = mockk<GoogleAuthApiService>()
    private lateinit var authRepository: AuthRepositoryImpl
    private val testScope = TestScope()

    @Before
    fun setup() {
        authRepository = AuthRepositoryImpl(userApiService, googleAuthApiService)
    }

    @Test
    fun `registerUser success returns success`() = runBlocking {
        val request = UserRegistrationDto(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User"
        )
        val userDto = UserDto(
            id = "123",
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            token = "jwt-token"
        )
        
        val mockResponse = Response.success(200, userDto)
        coEvery { userApiService.registerUser(request) } returns mockResponse

        val result = authRepository.registerUser("testuser", "test@example.com", "password123", "Test", "User")
        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(userDto, result.getOrNull())
    }

    @Test
    fun `registerUser with status 201 returns success`() = runBlocking {
        val request = UserRegistrationDto(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User"
        )
        val userDto = UserDto(
            id = "123",
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            token = "jwt-token"
        )
        
        val mockResponse = Response.success(201, userDto)
        coEvery { userApiService.registerUser(request) } returns mockResponse

        val result = authRepository.registerUser("testuser", "test@example.com", "password123", "Test", "User")
        testScope.advanceUntilIdle()

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
            lastName = "User"
        )
        
        val mockResponse = Response.success(200, null)
        coEvery { userApiService.registerUser(request) } returns mockResponse

        val result = authRepository.registerUser("testuser", "test@example.com", "password123", "Test", "User")
        testScope.advanceUntilIdle()

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
            lastName = "User"
        )
        
        val mockResponse = Response.error(400, "Bad request".toResponseBody(null))
        coEvery { userApiService.registerUser(request) } returns mockResponse

        val result = authRepository.registerUser("testuser", "test@example.com", "password123", "Test", "User")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Bad request", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerUser with custom error message returns failure`() = runBlocking {
        val request = UserRegistrationDto(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User"
        )
        
        val mockResponse = Response.error(422, "Email already exists".toResponseBody(null))
        coEvery { userApiService.registerUser(request) } returns mockResponse

        val result = authRepository.registerUser("testuser", "test@example.com", "password123", "Test", "User")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Email already exists", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerUser with network exception returns failure`() = runBlocking {
        coEvery { userApiService.registerUser(any()) } throws RuntimeException("Network error")

        val result = authRepository.registerUser("testuser", "test@example.com", "password123", "Test", "User")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginUser success returns success with merged token`() = runBlocking {
        val request = UserLoginDto(
            usernameOrEmail = "test@example.com",
            password = "password123"
        )
        val userDto = UserDto(
            id = "123",
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            token = null // Will be merged
        )
        val mergedUserDto = userDto.copy(token = "jwt-token")
        
        val mockResponse = Response.success(200, userDto)
        coEvery { userApiService.loginUser(request) } returns mockResponse

        val result = authRepository.loginUser("test@example.com", "password123")
        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(mergedUserDto, result.getOrNull())
        assertEquals("jwt-token", result.getOrNull()?.token)
    }

    @Test
    fun `loginUser with null data returns failure`() = runBlocking {
        val request = UserLoginDto(
            usernameOrEmail = "test@example.com",
            password = "password123"
        )
        
        val mockResponse = Response.success(200, null)
        coEvery { userApiService.loginUser(request) } returns mockResponse

        val result = authRepository.loginUser("test@example.com", "password123")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Login failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginUser with error status returns failure`() = runBlocking {
        val request = UserLoginDto(
            usernameOrEmail = "test@example.com",
            password = "password123"
        )
        
        val mockResponse = Response.error(401, "Unauthorized".toResponseBody(null))
        coEvery { userApiService.loginUser(request) } returns mockResponse

        val result = authRepository.loginUser("test@example.com", "password123")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginUser with network exception returns failure`() = runBlocking {
        coEvery { userApiService.loginUser(any()) } throws RuntimeException("Network timeout")

        val result = authRepository.loginUser("test@example.com", "password123")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Network timeout", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exchangeGoogleCode success returns success`() = runBlocking {
        val request = GoogleServerAuthRequest(code = "auth-code")
        val userDto = UserDto(
            id = "123",
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            token = "google-jwt-token"
        )
        
        val mockResponse = Response.success(200, userDto)
        coEvery { googleAuthApiService.exchangeGoogleServerAuthCode(request) } returns mockResponse

        val result = authRepository.exchangeGoogleCode("auth-code", "state")
        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(userDto, result.getOrNull())
    }

    @Test
    fun `exchangeGoogleCode with null data returns failure`() = runBlocking {
        val request = GoogleServerAuthRequest(code = "auth-code")
        
        val mockResponse = Response.success(200, null)
        coEvery { googleAuthApiService.exchangeGoogleServerAuthCode(request) } returns mockResponse

        val result = authRepository.exchangeGoogleCode("auth-code", "state")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Google OAuth2 failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exchangeGoogleCode with error status returns failure`() = runBlocking {
        val request = GoogleServerAuthRequest(code = "invalid-code")
        
        val mockResponse = Response.error(400, "Invalid code".toResponseBody(null))
        coEvery { googleAuthApiService.exchangeGoogleServerAuthCode(request) } returns mockResponse

        val result = authRepository.exchangeGoogleCode("invalid-code", "state")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Invalid code", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exchangeGoogleCode with network exception returns failure`() = runBlocking {
        coEvery { googleAuthApiService.exchangeGoogleServerAuthCode(any()) } throws RuntimeException("Network error")

        val result = authRepository.exchangeGoogleCode("auth-code", "state")
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `initiateGoogleLogin with 302 status returns success`() = runBlocking {
        val mockResponse = Response.error(302, "Redirect".toResponseBody(null))
        val headers = Headers.Builder().add("Location", "https://accounts.google.com/oauth/authorize").build()
        every { mockResponse.headers() } returns headers
        
        coEvery { googleAuthApiService.initiateGoogleLogin() } returns mockResponse

        val result = authRepository.initiateGoogleLogin()
        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("https://accounts.google.com/oauth/authorize", result.getOrNull())
    }

    @Test
    fun `initiateGoogleLogin with 301 status returns success`() = runBlocking {
        val mockResponse = Response.error(301, "Moved Permanently".toResponseBody(null))
        val headers = Headers.Builder().add("Location", "https://accounts.google.com/oauth/authorize").build()
        every { mockResponse.headers() } returns headers
        
        coEvery { googleAuthApiService.initiateGoogleLogin() } returns mockResponse

        val result = authRepository.initiateGoogleLogin()
        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("https://accounts.google.com/oauth/authorize", result.getOrNull())
    }

    @Test
    fun `initiateGoogleLogin with 200 status returns success`() = runBlocking {
        val mockResponse = Response.success(200, "OK")
        val headers = Headers.Builder().add("Location", "https://accounts.google.com/oauth/authorize").build()
        every { mockResponse.headers() } returns headers
        
        coEvery { googleAuthApiService.initiateGoogleLogin() } returns mockResponse

        val result = authRepository.initiateGoogleLogin()
        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals("https://accounts.google.com/oauth/authorize", result.getOrNull())
    }

    @Test
    fun `initiateGoogleLogin with missing location returns failure`() = runBlocking {
        val mockResponse = Response.success(200, "OK")
        val headers = Headers.Builder().build()
        every { mockResponse.headers() } returns headers
        
        coEvery { googleAuthApiService.initiateGoogleLogin() } returns mockResponse

        val result = authRepository.initiateGoogleLogin()
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("No se pudo obtener la URL de redirección (Status: 200)", result.exceptionOrNull()?.message)
    }

    @Test
    fun `initiateGoogleLogin with network exception returns failure`() = runBlocking {
        coEvery { googleAuthApiService.initiateGoogleLogin() } throws RuntimeException("Network error")

        val result = authRepository.initiateGoogleLogin()
        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `multiple registerUser calls work correctly`() = runBlocking {
        val request1 = UserRegistrationDto(
            username = "testuser1",
            email = "test1@example.com",
            password = "password123",
            firstName = "Test1",
            lastName = "User1"
        )
        val userDto1 = UserDto(
            id = "123",
            username = "testuser1",
            email = "test1@example.com",
            firstName = "Test1",
            lastName = "User1",
            token = "jwt-token1"
        )
        
        val request2 = UserRegistrationDto(
            username = "testuser2",
            email = "test2@example.com",
            password = "password456",
            firstName = "Test2",
            lastName = "User2"
        )
        val userDto2 = UserDto(
            id = "456",
            username = "testuser2",
            email = "test2@example.com",
            firstName = "Test2",
            lastName = "User2",
            token = "jwt-token2"
        )
        
        val mockResponse1 = Response.success(201, userDto1)
        val mockResponse2 = Response.success(200, userDto2)
        coEvery { userApiService.registerUser(request1) } returns mockResponse1
        coEvery { userApiService.registerUser(request2) } returns mockResponse2

        val result1 = authRepository.registerUser("testuser1", "test1@example.com", "password123", "Test1", "User1")
        val result2 = authRepository.registerUser("testuser2", "test2@example.com", "password456", "Test2", "User2")
        testScope.advanceUntilIdle()

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertEquals(userDto1, result1.getOrNull())
        assertEquals(userDto2, result2.getOrNull())
    }

    @Test
    fun `multiple loginUser calls work correctly`() = runBlocking {
        val request1 = UserLoginDto(
            usernameOrEmail = "test1@example.com",
            password = "password123"
        )
        val userDto1 = UserDto(
            id = "123",
            username = "testuser1",
            email = "test1@example.com",
            firstName = "Test1",
            lastName = "User1",
            token = "jwt-token1"
        )
        
        val request2 = UserLoginDto(
            usernameOrEmail = "test2@example.com",
            password = "password456"
        )
        val userDto2 = UserDto(
            id = "456",
            username = "testuser2",
            email = "test2@example.com",
            firstName = "Test2",
            lastName = "User2",
            token = "jwt-token2"
        )
        
        val mockResponse1 = Response.success(200, userDto1)
        val mockResponse2 = Response.success(200, userDto2)
        coEvery { userApiService.loginUser(request1) } returns mockResponse1
        coEvery { userApiService.loginUser(request2) } returns mockResponse2

        val result1 = authRepository.loginUser("test1@example.com", "password123")
        val result2 = authRepository.loginUser("test2@example.com", "password456")
        testScope.advanceUntilIdle()

        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertEquals("jwt-token1", result1.getOrNull()?.token)
        assertEquals("jwt-token2", result2.getOrNull()?.token)
    }
}
