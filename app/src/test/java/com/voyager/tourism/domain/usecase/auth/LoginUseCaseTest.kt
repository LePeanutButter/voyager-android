package com.voyager.tourism.domain.usecase.auth

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.local.TokenManager
import com.voyager.tourism.domain.repository.AuthRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private val authRepository = mockk<AuthRepository>()
    private val tokenManager = mockk<TokenManager>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>(relaxed = true)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        useCase = LoginUseCase(authRepository, tokenManager, preferencesManager, moshi, UserMapper())
    }

    @Test
    fun `invoke fails when username blank`() = runTest {
        val r = useCase("   ", "secret")
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `invoke fails when password blank`() = runTest {
        val r = useCase("user@mail.com", " ")
        assertTrue(r.isFailure)
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
    fun `loginWithGoogle success`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { authRepository.handleGoogleCallback("code", "state") } returns Result.success(dto)

        val r = useCase.loginWithGoogle("code", "state")

        assertTrue(r.isSuccess)
        verify { tokenManager.saveToken("jwt-token") }
        verify { preferencesManager.saveCurrentUserId("42") }
    }

    @Test
    fun `loginWithGoogle failure`() = runTest {
        coEvery { authRepository.handleGoogleCallback(any(), any()) } returns Result.failure(Exception("oauth"))

        val r = useCase.loginWithGoogle("c", null)

        assertTrue(r.isFailure)
        assertEquals("oauth", r.exceptionOrNull()?.message)
    }
}
