package com.voyager.tourism.domain.usecase.auth

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.local.TokenManager
import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.domain.repository.AuthRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
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
    private val userMapper = UserMapper()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        useCase = LoginUseCase(authRepository, tokenManager, preferencesManager, moshi, userMapper)
    }

    @Test
    fun `fails when input blank`() = runTest {
        assertTrue(useCase("", "p").isFailure)
        assertTrue(useCase("u", "").isFailure)
    }

    @Test
    fun `invoke success persists data and returns domain user`() = runTest {
        val dto = TestFixtures.userDto(id = 7L, token = "jwt")
        coEvery { authRepository.loginUser("u", "p") } returns Result.success(dto)

        val result = useCase("u", "p")

        assertTrue(result.isSuccess)
        assertEquals("7", result.getOrThrow().id)
        verify { tokenManager.saveToken("jwt") }
        verify { preferencesManager.saveCurrentUserId("7") }
        verify { tokenManager.saveUser(any()) }
    }

    @Test
    fun `invoke failure returns error`() = runTest {
        coEvery { authRepository.loginUser(any(), any()) } returns Result.failure(Exception("bad"))
        val result = useCase("u", "p")
        assertTrue(result.isFailure)
        assertEquals("bad", result.exceptionOrNull()?.message)
    }

    @Test
    fun `loginWithGoogle success`() = runTest {
        val dto = TestFixtures.userDto(id = 8L, token = "jwt-g")
        coEvery { authRepository.exchangeGoogleCode("c", "s") } returns Result.success(dto)

        val result = useCase.loginWithGoogle("c", "s")

        assertTrue(result.isSuccess)
        verify { tokenManager.saveToken("jwt-g") }
    }

    @Test
    fun `loginWithGoogle failure`() = runTest {
        coEvery { authRepository.exchangeGoogleCode(any(), any()) } returns Result.failure(Exception("oauth fail"))
        val result = useCase.loginWithGoogle("c")
        assertTrue(result.isFailure)
    }
}
