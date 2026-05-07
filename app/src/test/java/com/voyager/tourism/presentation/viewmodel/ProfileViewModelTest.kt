package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.TokenManager
import com.voyager.tourism.domain.usecase.auth.GetUserUseCase
import com.voyager.tourism.domain.usecase.auth.UpdateProfileUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getUserUseCase = mockk<GetUserUseCase>()
    private val updateProfileUseCase = mockk<UpdateProfileUseCase>()
    private val tokenManager = mockk<TokenManager>()
    private lateinit var vm: ProfileViewModel

    @Before
    fun setup() {
        vm = ProfileViewModel(getUserUseCase, updateProfileUseCase, tokenManager)
    }

    @Test
    fun `loadProfile success`() {
        val dto = TestFixtures.userDto()
        coEvery { getUserUseCase("42") } returns Result.success(dto)

        vm.loadProfile("42")

        val state = vm.uiState.value
        assertTrue(state is ProfileUiState.Loaded)
        assertEquals(dto, (state as ProfileUiState.Loaded).user)
    }

    @Test
    fun `loadProfile failure`() {
        coEvery { getUserUseCase(any()) } returns Result.failure(RuntimeException("missing"))

        vm.loadProfile("1")

        assertTrue(vm.uiState.value is ProfileUiState.Error)
    }

    @Test
    fun `saveProfile success shows SaveSuccess`() {
        val dto = TestFixtures.userDto()
        coEvery {
            updateProfileUseCase("42", "A", "B", "bio", null, listOf("x"))
        } returns Result.success(dto)

        vm.saveProfile("42", "A", "B", "bio", null, listOf("x"))

        assertTrue(vm.uiState.value is ProfileUiState.SaveSuccess)
    }

    @Test
    fun `saveProfile failure`() {
        coEvery { updateProfileUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(IllegalArgumentException("bad"))

        vm.saveProfile("1", "A", "B", "bio", null, emptyList())

        assertTrue(vm.uiState.value is ProfileUiState.Error)
    }

    @Test
    fun `getCurrentUserId parses string id from json`() {
        coEvery { tokenManager.getUser() } returns """{"id":"99","email":"x"}"""

        assertEquals("99", vm.getCurrentUserId())
    }

    @Test
    fun `getCurrentUserId null when no json`() {
        coEvery { tokenManager.getUser() } returns null

        assertNull(vm.getCurrentUserId())
    }

    @Test
    fun `resetState`() {
        vm.resetState()
        assertTrue(vm.uiState.value is ProfileUiState.Loading)
    }
}
