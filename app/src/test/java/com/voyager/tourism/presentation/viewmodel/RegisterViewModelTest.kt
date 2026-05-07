package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.usecase.auth.RegisterUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val registerUseCase = mockk<RegisterUseCase>()
    private lateinit var vm: RegisterViewModel

    @Before
    fun setup() {
        vm = RegisterViewModel(registerUseCase)
    }

    @Test
    fun `register success`() {
        coEvery {
            registerUseCase("u", "e@e.com", "p", "F", "L")
        } returns Result.success(TestFixtures.domainUser())

        vm.register("u", "e@e.com", "p", "F", "L")

        assertTrue(vm.uiState.value is RegisterUiState.Success)
    }

    @Test
    fun `register failure`() {
        coEvery { registerUseCase(any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("taken"))

        vm.register("u", "e", "p", "F", "L")

        assertTrue(vm.uiState.value is RegisterUiState.Error)
    }

    @Test
    fun `resetState`() {
        vm.resetState()
        assertTrue(vm.uiState.value is RegisterUiState.Idle)
    }
}
