package com.voyager.tourism.presentation.viewmodel

import android.app.Application
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.voyager.tourism.domain.repository.AuthRepository
import com.voyager.tourism.domain.usecase.auth.LoginUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase = mockk<LoginUseCase>()
    private val authRepository = mockk<AuthRepository>()
    private lateinit var vm: LoginViewModel

    @Before
    fun setup() {
        vm = LoginViewModel(loginUseCase, authRepository)
    }

    @Test
    fun `login success`() {
        coEvery { loginUseCase("u", "p") } returns Result.success(TestFixtures.domainUser())
        vm.login("u", "p")
        assertTrue(vm.uiState.value is LoginUiState.Success)
    }

    @Test
    fun `login failure`() {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(RuntimeException("x"))
        vm.login("u", "p")
        assertTrue(vm.uiState.value is LoginUiState.Error)
        assertEquals("x", (vm.uiState.value as LoginUiState.Error).message)
    }

    @Test
    fun `handleGoogleSignInResult success uses serverAuthCode`() {
        val account = mockk<GoogleSignInAccount>()
        every { account.serverAuthCode } returns "srv-code"
        val task = mockk<Task<GoogleSignInAccount>>()
        every { task.getResult(ApiException::class.java) } returns account
        coEvery { loginUseCase.loginWithGoogle("srv-code", null) } returns Result.success(TestFixtures.domainUser())

        vm.handleGoogleSignInResult(task)

        assertTrue(vm.uiState.value is LoginUiState.Success)
    }

    @Test
    fun `handleGoogleSignInResult error when serverAuthCode null`() {
        val account = mockk<GoogleSignInAccount>()
        every { account.serverAuthCode } returns null
        val task = mockk<Task<GoogleSignInAccount>>()
        every { task.getResult(ApiException::class.java) } returns account

        vm.handleGoogleSignInResult(task)

        assertTrue(vm.uiState.value is LoginUiState.Error)
    }

    @Test
    fun `handleGoogleCallback missing code`() {
        val uri = Uri.parse("https://app/callback")
        vm.handleGoogleCallback(uri)
        assertTrue(vm.uiState.value is LoginUiState.Error)
    }

    @Test
    fun `handleGoogleCallback success`() {
        coEvery { loginUseCase.loginWithGoogle("c", "s") } returns Result.success(TestFixtures.domainUser())
        val uri = Uri.parse("https://app/callback?code=c&state=s")
        vm.handleGoogleCallback(uri)
        assertTrue(vm.uiState.value is LoginUiState.Success)
    }

    @Test
    fun `handleGoogleCallback login failure`() {
        coEvery { loginUseCase.loginWithGoogle("c", null) } returns Result.failure(RuntimeException("oauth"))
        val uri = Uri.parse("https://app/callback?code=c")
        vm.handleGoogleCallback(uri)
        assertTrue(vm.uiState.value is LoginUiState.Error)
        assertEquals("oauth", (vm.uiState.value as LoginUiState.Error).message)
    }

    @Test
    fun `handleGoogleCallback unexpected exception sets error`() {
        coEvery { loginUseCase.loginWithGoogle(any(), any()) } throws RuntimeException("boom")
        val uri = Uri.parse("https://app/callback?code=c&state=s")
        vm.handleGoogleCallback(uri)
        assertTrue(vm.uiState.value is LoginUiState.Error)
    }

    @Test
    fun `resetState`() {
        vm.resetState()
        assertTrue(vm.uiState.value is LoginUiState.Idle)
    }
}
