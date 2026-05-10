package com.voyager.tourism.domain.usecase.auth

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.local.TokenManager
import com.voyager.tourism.domain.repository.AuthRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginUseCaseTest {

    private val authRepository = mockk<AuthRepository>()
    private val tokenManager = mockk<TokenManager>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>(relaxed = true)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private lateinit var useCase: LoginUseCase
    private val testScope = TestScope()

    @Before
    fun setup() {
        useCase = LoginUseCase(authRepository, tokenManager, preferencesManager, moshi, UserMapper())
    }

    @Test
    fun `invoke fails when username blank`() = runTest {
        val r = useCase("   ", "secret")
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Username or email cannot be empty", r.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke fails when password blank`() = runTest {
        val r = useCase("user@mail.com", " ")
        assertTrue(r.isFailure)
        assertEquals("Password cannot be empty", r.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke fails when both blank`() = runTest {
        val r = useCase("", "")
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `invoke success persists token and user id`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { authRepository.loginUser("u", "p") } returns Result.success(dto)

        val r = useCase("u", "p")

        assertTrue(r.isSuccess)
        assertEquals("42", r.getOrNull()?.id)
        verify { tokenManager.saveToken("jwt-token") }
        verify { preferencesManager.saveCurrentUserId("42") }
        verify { tokenManager.saveUser(any()) }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        coEvery { authRepository.loginUser(any(), any()) } returns Result.failure(RuntimeException("nope"))

        val r = useCase("u", "p")

        assertTrue(r.isFailure)
        assertEquals("nope", r.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with network error`() = runTest {
        coEvery { authRepository.loginUser(any(), any()) } returns Result.failure(RuntimeException("Network timeout"))

        val r = useCase("u", "p")

        assertTrue(r.isFailure)
        assertEquals("Network timeout", r.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithGoogle success`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { authRepository.exchangeGoogleCode("code", "state") } returns Result.success(dto)

        val r = useCase.loginWithGoogle("code", "state")

        assertTrue(r.isSuccess)
        verify { tokenManager.saveToken("jwt-token") }
        verify { preferencesManager.saveCurrentUserId("42") }
    }

    @Test
    fun `loginWithGoogle failure`() = runTest {
        coEvery { authRepository.exchangeGoogleCode(any(), any()) } returns Result.failure(Exception("oauth"))

        val r = useCase.loginWithGoogle("c", null)

        assertTrue(r.isFailure)
        assertEquals("oauth", r.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithGoogle with invalid code`() = runTest {
        coEvery { authRepository.exchangeGoogleCode(any(), any()) } returns Result.failure(RuntimeException("Invalid code"))

        val r = useCase.loginWithGoogle("invalid-code", "state")

        assertTrue(r.isFailure)
        assertEquals("Invalid code", r.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithGoogle with network error`() = runTest {
        coEvery { authRepository.exchangeGoogleCode(any(), any()) } returns Result.failure(RuntimeException("Network error"))

        val r = useCase.loginWithGoogle("code", "state")

        assertTrue(r.isFailure)
        assertEquals("Network error", r.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithGoogle with null state`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { authRepository.exchangeGoogleCode("code", null) } returns Result.success(dto)

        val r = useCase.loginWithGoogle("code", null)

        assertTrue(r.isSuccess)
        verify { tokenManager.saveToken("jwt-token") }
        verify { preferencesManager.saveCurrentUserId("42") }
    }

    @Test
    fun `multiple login calls work correctly`() = runTest {
        val dto1 = TestFixtures.userDto()
        val dto2 = TestFixtures.userDto()
        coEvery { authRepository.loginUser("u1", "p1") } returns Result.success(dto1)
        coEvery { authRepository.loginUser("u2", "p2") } returns Result.success(dto2)

        val r1 = useCase("u1", "p1")
        val r2 = useCase("u2", "p2")

        assertTrue(r1.isSuccess)
        assertTrue(r2.isSuccess)
        assertEquals("42", r1.getOrNull()?.id)
        assertEquals("42", r2.getOrNull()?.id)
    }

    @Test
    fun `login with special characters in username`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { authRepository.loginUser("user@domain.com", "pass") } returns Result.success(dto)

        val r = useCase("user@domain.com", "pass")

        assertTrue(r.isSuccess)
        assertEquals("42", r.getOrNull()?.id)
    }

    @Test
    fun `login with very long password`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { authRepository.loginUser("user", "very-long-password") } returns Result.success(dto)

        val r = useCase("user", "very-long-password")

        assertTrue(r.isSuccess)
        assertEquals("42", r.getOrNull()?.id)
    }

    @Test
    fun `login with whitespace only username`() = runTest {
        val r = useCase("    ", "password")
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `login with whitespace only password`() = runTest {
        val r = useCase("username", "    ")
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `invoke success without token skips saveToken`() = runTest {
        val dto = TestFixtures.userDto(token = null)
        coEvery { authRepository.loginUser("u", "p") } returns Result.success(dto)

        val r = useCase("u", "p")

        assertTrue(r.isSuccess)
        verify(exactly = 0) { tokenManager.saveToken(any()) }
        verify { preferencesManager.saveCurrentUserId("42") }
        verify { tokenManager.saveUser(any()) }
    }

    @Test
    fun `invoke catches unexpected exception from repository`() = runTest {
        coEvery { authRepository.loginUser(any(), any()) } throws IllegalStateException("db")

        val r = useCase("u", "p")

        assertTrue(r.isFailure)
        assertEquals("db", r.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithGoogle catches repository throw`() = runTest {
        coEvery { authRepository.exchangeGoogleCode(any(), any()) } throws IllegalStateException("oauth-down")

        val r = useCase.loginWithGoogle("c", "s")

        assertTrue(r.isFailure)
        assertEquals("oauth-down", r.exceptionOrNull()?.message)
    }
}
