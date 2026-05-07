package com.voyager.tourism.domain.usecase.auth

import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateProfileUseCaseTest {

    private val repository = mockk<UserRepository>()
    private lateinit var useCase: UpdateProfileUseCase

    @Before
    fun setup() {
        useCase = UpdateProfileUseCase(repository)
    }

    @Test
    fun `blank user id fails`() = runTest {
        val r = useCase(" ", "A", "B", "bio", null, emptyList())
        assertTrue(r.isFailure)
        assertEquals("User ID cannot be empty", r.exceptionOrNull()?.message)
    }

    @Test
    fun `blank first name fails`() = runTest {
        val r = useCase("1", " ", "B", "bio", null, emptyList())
        assertTrue(r.isFailure)
        assertEquals("First name is required", r.exceptionOrNull()?.message)
    }

    @Test
    fun `blank last name fails`() = runTest {
        val r = useCase("1", "A", "  ", "bio", null, emptyList())
        assertTrue(r.isFailure)
    }

    @Test
    fun `blank bio fails`() = runTest {
        val r = useCase("1", "A", "B", "  ", null, emptyList())
        assertTrue(r.isFailure)
        assertEquals("Biography is required", r.exceptionOrNull()?.message)
    }

    @Test
    fun `bio too long fails`() = runTest {
        val r = useCase("1", "A", "B", "x".repeat(501), null, emptyList())
        assertTrue(r.isFailure)
        assertEquals("Biography must be 500 characters or less", r.exceptionOrNull()?.message)
    }

    @Test
    fun `success delegates to repository`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery {
            repository.updateUser("42", "A", "B", "bio", "+1", listOf("art"))
        } returns Result.success(dto)

        val r = useCase("42", "A", "B", "bio", "+1", listOf("art"))

        assertTrue(r.isSuccess)
        assertEquals(dto, r.getOrThrow())
    }

    @Test
    fun `repository exception becomes failure`() = runTest {
        coEvery { repository.updateUser(any(), any(), any(), any(), any(), any()) } throws RuntimeException("x")

        val r = useCase("1", "A", "B", "bio", null, emptyList())

        assertTrue(r.isFailure)
        assertEquals("x", r.exceptionOrNull()?.message)
    }
}
