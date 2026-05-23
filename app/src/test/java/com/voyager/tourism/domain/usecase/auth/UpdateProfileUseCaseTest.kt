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
    fun `validation fails on blank fields`() = runTest {
        assertTrue(useCase("", "A", "B", "bio", null, emptyList()).isFailure)
        assertTrue(useCase("1", "", "B", "bio", null, emptyList()).isFailure)
        assertTrue(useCase("1", "A", "", "bio", null, emptyList()).isFailure)
        assertTrue(useCase("1", "A", "B", "", null, emptyList()).isFailure)
    }

    @Test
    fun `validation fails when bio too long`() = runTest {
        val longBio = "a".repeat(501)
        val r = useCase("1", "A", "B", longBio, null, emptyList())
        assertTrue(r.isFailure)
    }

    @Test
    fun `invoke success calls repository`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { repository.updateUser("1", "A", "B", "bio", null, any()) } returns Result.success(dto)

        val result = useCase("1", "A", "B", "bio", null, emptyList())

        assertTrue(result.isSuccess)
        assertEquals(dto, result.getOrNull())
    }

    @Test
    fun `invoke failure returns repository error`() = runTest {
        coEvery { repository.updateUser(any(), any(), any(), any(), any(), any()) } returns Result.failure(Exception("err"))
        assertTrue(useCase("1", "A", "B", "bio", null, emptyList()).isFailure)
    }
}
