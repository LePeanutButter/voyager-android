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
    fun `blank user id fails`() = runTest {
        val r = useCase("  ")
        assertTrue(r.isFailure)
        assertEquals("User ID cannot be empty", r.exceptionOrNull()?.message)
    }

    @Test
    fun `success maps domain user to dto`() = runTest {
        val domain = TestFixtures.domainUser()
        coEvery { userRepository.getUserById("42") } returns Result.success(domain)

        val r = useCase("42")

        assertTrue(r.isSuccess)
        val dto = r.getOrThrow()
        assertEquals(42L, dto.id)
        assertEquals(domain.email, dto.email)
    }

    @Test
    fun `null user after success is not found`() = runTest {
        coEvery { userRepository.getUserById("1") } returns Result.success(null)

        val r = useCase("1")

        assertTrue(r.isFailure)
        assertEquals("User not found", r.exceptionOrNull()?.message)
    }

    @Test
    fun `repository failure propagates`() = runTest {
        coEvery { userRepository.getUserById(any()) } returns Result.failure(RuntimeException("net"))

        val r = useCase("99")

        assertTrue(r.isFailure)
        assertEquals("net", r.exceptionOrNull()?.message)
    }
}
