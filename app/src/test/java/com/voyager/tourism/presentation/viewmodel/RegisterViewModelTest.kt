package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.usecase.auth.RegisterUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val registerUseCase = mockk<RegisterUseCase>()
    private lateinit var vm: RegisterViewModel

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        vm = RegisterViewModel(registerUseCase)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
    }

    @Test
    fun `register success`() = runTest {
        coEvery {
            registerUseCase("u", "e@e.com", "p", "F", "L")
        } returns Result.success(TestFixtures.domainUser())

        vm.register("u", "e@e.com", "p", "F", "L")
        advanceUntilIdle()

        assertTrue(vm.uiState.value is RegisterUiState.Success)
    }

    @Test
    fun `register failure`() = runTest {
        coEvery { registerUseCase(any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("taken"))

        vm.register("u", "e", "p", "F", "L")
        advanceUntilIdle()

        assertTrue(vm.uiState.value is RegisterUiState.Error)
    }

    @Test
    fun `resetState`() {
        vm.resetState()
        assertTrue(vm.uiState.value is RegisterUiState.Idle)
    }
}
