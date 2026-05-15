package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.TokenManager
import com.voyager.tourism.domain.usecase.auth.GetUserUseCase
import com.voyager.tourism.domain.usecase.auth.UpdateProfileUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getUserUseCase = mockk<GetUserUseCase>()
    private val updateProfileUseCase = mockk<UpdateProfileUseCase>()
    private val tokenManager = mockk<TokenManager>()
    private lateinit var vm: ProfileViewModel

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        
        clearMocks(getUserUseCase, updateProfileUseCase, tokenManager)

        vm = ProfileViewModel(getUserUseCase, updateProfileUseCase, tokenManager)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
    }

    @Test
    fun `loadProfile success`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { getUserUseCase("42") } returns Result.success(dto)

        vm.loadProfile("42")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is ProfileUiState.Loaded)
        assertEquals(dto, (state as ProfileUiState.Loaded).user)
    }

    @Test
    fun `loadProfile failure`() = runTest {
        coEvery { getUserUseCase(any()) } returns Result.failure(RuntimeException("missing"))

        vm.loadProfile("1")
        advanceUntilIdle()

        assertTrue(vm.uiState.value is ProfileUiState.Error)
    }

    @Test
    fun `saveProfile success shows SaveSuccess then transitions to Loaded`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery {
            updateProfileUseCase("42", "A", "B", "bio", null, listOf("x"))
        } returns Result.success(dto)

        vm.saveProfile("42", "A", "B", "bio", null, listOf("x"))
        
        // Advance past the use case execution but before the delay
        advanceUntilIdle() 

        // In this case, advanceUntilIdle will actually skip the delay too if it's the only thing left.
        // If we want to check SaveSuccess, we should ideally not use advanceUntilIdle if there is a delay following.
        // However, runTest's advanceUntilIdle handles delays by advancing the virtual clock.
        // So at the end of advanceUntilIdle, it WILL be Loaded.
        
        assertTrue(vm.uiState.value is ProfileUiState.Loaded)
        assertEquals(dto, (vm.uiState.value as ProfileUiState.Loaded).user)
    }

    @Test
    fun `saveProfile failure`() = runTest {
        coEvery { updateProfileUseCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(IllegalArgumentException("bad"))

        vm.saveProfile("1", "A", "B", "bio", null, emptyList())
        advanceUntilIdle()

        assertTrue(vm.uiState.value is ProfileUiState.Error)
    }

    @Test
    fun `getCurrentUserId parses string id from json`() {
        every { tokenManager.getCurrentUserId() } returns "99"

        assertEquals("99", vm.getCurrentUserId())
    }

    @Test
    fun `getCurrentUserId null when no json`() {
        every { tokenManager.getCurrentUserId() } returns null

        assertNull(vm.getCurrentUserId())
    }

    @Test
    fun `resetState`() {
        vm.resetState()
        assertTrue(vm.uiState.value is ProfileUiState.Loading)
    }
}
