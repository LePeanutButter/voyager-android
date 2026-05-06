package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.session.SessionInvalidationNotifier
import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.domain.usecase.auth.LoginUseCase
import com.voyager.tourism.domain.usecase.auth.RegisterUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase = mockk<LoginUseCase>(relaxed = true)
    private val registerUseCase = mockk<RegisterUseCase>(relaxed = true)
    private val userRepository = mockk<UserRepository>()
    private lateinit var sessionNotifier: SessionInvalidationNotifier

    @Before
    fun setup() {
        sessionNotifier = SessionInvalidationNotifier()
        coEvery { userRepository.getCurrentUser() } returns Result.success(null)
    }

    private fun viewModel() = AuthViewModel(
        loginUseCase,
        registerUseCase,
        userRepository,
        sessionNotifier,
    )

    @Test
    fun `starts unauthenticated when no stored user`() {
        val vm = viewModel()
        assertTrue(vm.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `starts authenticated when user in repository`() {
        coEvery { userRepository.getCurrentUser() } returns Result.success(TestFixtures.domainUser())
        val vm = viewModel()
        assertTrue(vm.authState.value is AuthState.Authenticated)
        assertEquals("42", vm.currentUser.value?.id)
    }

    @Test
    fun `getCurrentUser failure yields unauthenticated`() {
        coEvery { userRepository.getCurrentUser() } returns Result.failure(RuntimeException("net"))
        val vm = viewModel()
        assertTrue(vm.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `login success`() {
        coEvery { loginUseCase("a@b.c", "x") } returns Result.success(TestFixtures.domainUser())
        val vm = viewModel()
        vm.login("a@b.c", "x")
        assertTrue(vm.authState.value is AuthState.Authenticated)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `login failure sets error state`() {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(RuntimeException("bad creds"))
        val vm = viewModel()
        vm.login("a", "b")
        assertTrue(vm.authState.value is AuthState.Error)
        assertEquals("bad creds", (vm.authState.value as AuthState.Error).message)
    }

    @Test
    fun `register success`() {
        coEvery {
            registerUseCase("e@e.com", "p", "u", "F", "L")
        } returns Result.success(TestFixtures.domainUser())
        val vm = viewModel()
        vm.register("e@e.com", "p", "u", "F", "L")
        assertTrue(vm.authState.value is AuthState.Authenticated)
    }

    @Test
    fun `logout clears user`() {
        coEvery { userRepository.logout() } returns Result.success(Unit)
        val vm = viewModel()
        vm.logout()
        assertTrue(vm.authState.value is AuthState.Unauthenticated)
        assertNull(vm.currentUser.value)
    }

    @Test
    fun `handleGoogleOAuthUri ignores null`() {
        val vm = viewModel()
        vm.handleGoogleOAuthUri(null)
        assertNull(vm.oauthError.value)
    }

    @Test
    fun `oauth error query sets oauthError`() {
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns "access_denied"
        every { uri.getQueryParameter("error_description") } returns "User cancelled"
        every { uri.getQueryParameter("code") } returns null
        val vm = viewModel()
        vm.handleGoogleOAuthUri(uri)
        assertEquals("User cancelled", vm.oauthError.value)
    }

    @Test
    fun `oauth missing code sets message`() {
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns null
        every { uri.getQueryParameter("code") } returns null
        val vm = viewModel()
        vm.handleGoogleOAuthUri(uri)
        assertTrue(vm.oauthError.value?.contains("código") == true)
    }

    @Test
    fun `oauth success navigates dashboard`() = runBlocking {
        coEvery { loginUseCase.loginWithGoogle("c", "s") } returns Result.success(TestFixtures.domainUser())
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns null
        every { uri.getQueryParameter("code") } returns "c"
        every { uri.getQueryParameter("state") } returns "s"
        val vm = viewModel()
        val nav = async { vm.navigateAfterAuth.first() }
        yield()
        vm.handleGoogleOAuthUri(uri)
        assertEquals(AuthViewModel.ROUTE_DASHBOARD, nav.await())
        assertTrue(vm.authState.value is AuthState.Authenticated)
    }

    @Test
    fun `clearOAuthError`() {
        val vm = viewModel()
        vm.clearOAuthError()
        assertNull(vm.oauthError.value)
    }

    @Test
    fun `clearError`() {
        val vm = viewModel()
        vm.clearError()
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `adoptAuthenticatedUser`() {
        val vm = viewModel()
        vm.adoptAuthenticatedUser(TestFixtures.domainUser())
        assertTrue(vm.authState.value is AuthState.Authenticated)
    }

    @Test
    fun `onSessionExpired emits login route`() = runBlocking {
        val vm = viewModel()
        val nav = async { vm.navigateAfterAuth.first() }
        yield()
        vm.onSessionExpired()
        assertEquals(AuthViewModel.ROUTE_LOGIN, nav.await())
        assertTrue(vm.authState.value is AuthState.Unauthenticated)
    }
}
