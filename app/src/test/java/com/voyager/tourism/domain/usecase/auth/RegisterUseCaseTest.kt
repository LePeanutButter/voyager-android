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

class RegisterUseCaseTest {

    private val authRepository = mockk<AuthRepository>()
    private val tokenManager = mockk<TokenManager>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>(relaxed = true)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private lateinit var useCase: RegisterUseCase

    @Before
    fun setup() {
        useCase = RegisterUseCase(authRepository, tokenManager, preferencesManager, moshi, UserMapper())
    }

    @Test
    fun `success saves session fields`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery {
            authRepository.registerUser("u", "e@e.com", "p", "F", "L")
        } returns Result.success(dto)

        val r = useCase("u", "e@e.com", "p", "F", "L")

        assertTrue(r.isSuccess)
        assertEquals("42", r.getOrNull()?.id)
        verify { tokenManager.saveToken("jwt-token") }
        verify { preferencesManager.saveCurrentUserId("42") }
        verify { tokenManager.saveUser(any()) }
    }

    @Test
    fun `failure from repository`() = runTest {
        coEvery { authRepository.registerUser(any(), any(), any(), any(), any()) } returns
            Result.failure(IllegalStateException("dup"))

        val r = useCase("u", "e@e.com", "p", "F", "L")

        assertTrue(r.isFailure)
        assertEquals("dup", r.exceptionOrNull()?.message)
    }
}
