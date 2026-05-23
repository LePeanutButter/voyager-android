package com.voyager.tourism.domain.usecase.auth

import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val userMapper = UserMapper()
    private lateinit var useCase: GetUserUseCase

    @Before
    fun setup() {
        useCase = GetUserUseCase(userRepository, userMapper)
    }

    @Test
    fun `fails when userId blank`() = runTest {
        assertTrue(useCase(" ").isFailure)
    }

    @Test
    fun `invoke success returns user dto`() = runTest {
        val domain = TestFixtures.domainUser()
        coEvery { userRepository.getUserById("42") } returns Result.success(domain)

        val result = useCase("42")

        assertTrue(result.isSuccess)
        assertEquals(domain.email, result.getOrThrow().email)
    }

    @Test
    fun `invoke returns failure when user not found`() = runTest {
        coEvery { userRepository.getUserById("1") } returns Result.success(null)
        val result = useCase("1")
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        coEvery { userRepository.getUserById(any()) } returns Result.failure(RuntimeException("net"))
        assertTrue(useCase("42").isFailure)
    }
}
