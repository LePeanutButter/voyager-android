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
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
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
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase = mockk<LoginUseCase>(relaxed = true)
    private val registerUseCase = mockk<RegisterUseCase>(relaxed = true)
    private val userRepository = mockk<UserRepository>()
    private lateinit var sessionNotifier: SessionInvalidationNotifier

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        sessionNotifier = SessionInvalidationNotifier()
        coEvery { userRepository.getCurrentUser() } returns Result.success(null)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
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
            registerUseCase("u", "e@e.com", "p", "F", "L")
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
    fun `oauth success navigates dashboard`() = runTest {
        coEvery { loginUseCase.loginWithGoogle("c", "s") } returns Result.success(TestFixtures.domainUser())
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns null
        every { uri.getQueryParameter("code") } returns "c"
        every { uri.getQueryParameter("state") } returns "s"
        val vm = viewModel()
        val nav = async { vm.navigateAfterAuth.first() }
        yield()
        vm.handleGoogleOAuthUri(uri)
        advanceUntilIdle()
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
    fun `onSessionExpired emits login route`() = runTest {
        val vm = viewModel()
        val nav = async { vm.navigateAfterAuth.first() }
        yield()
        vm.onSessionExpired()
        advanceUntilIdle()
        assertEquals(AuthViewModel.ROUTE_LOGIN, nav.await())
        assertTrue(vm.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `register failure sets error state`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { registerUseCase(any(), any(), any(), any(), any()) } returns Result.failure(
            RuntimeException("Registration failed"),
        )
        val vm = viewModel()
        vm.register("test@test.com", "pass", "user", "first", "last")
        advanceUntilIdle()

        assertTrue(vm.authState.value is AuthState.Error)
        assertEquals("Registration failed", (vm.authState.value as AuthState.Error).message)
    }

    @Test
    fun `register with invalid email`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { registerUseCase(any(), any(), any(), any(), any()) } returns Result.failure(
            RuntimeException("Invalid email"),
        )
        val vm = viewModel()
        vm.register("invalid-email", "pass", "user", "first", "last")
        advanceUntilIdle()

        assertTrue(vm.authState.value is AuthState.Error)
        assertEquals("Invalid email", (vm.authState.value as AuthState.Error).message)
    }

    @Test
    fun `register with weak password`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { registerUseCase(any(), any(), any(), any(), any()) } returns Result.failure(
            RuntimeException("Password too weak"),
        )
        val vm = viewModel()
        vm.register("test@test.com", "weak", "user", "first", "last")
        advanceUntilIdle()

        assertTrue(vm.authState.value is AuthState.Error)
        assertEquals("Password too weak", (vm.authState.value as AuthState.Error).message)
    }

    @Test
    fun `login with empty credentials`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(RuntimeException("Empty credentials"))
        val vm = viewModel()
        vm.login("", "")
        advanceUntilIdle()

        assertTrue(vm.authState.value is AuthState.Error)
        assertEquals("Empty credentials", (vm.authState.value as AuthState.Error).message)
    }

    @Test
    fun `login with network error`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(RuntimeException("Network timeout"))
        val vm = viewModel()
        vm.login("test@test.com", "password")
        advanceUntilIdle()

        assertTrue(vm.authState.value is AuthState.Error)
        assertEquals("Network timeout", (vm.authState.value as AuthState.Error).message)
    }

    @Test
    fun `logout with error`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { userRepository.logout() } returns Result.failure(RuntimeException("Logout failed"))
        val vm = viewModel()
        vm.logout()
        advanceUntilIdle()

        assertTrue(vm.authState.value is AuthState.Unauthenticated)
        assertNull(vm.currentUser.value)
    }

    @Test
    fun `oauth with state parameter`() = runTest {
        coEvery { loginUseCase.loginWithGoogle("c", "custom-state") } returns Result.success(TestFixtures.domainUser())
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns null
        every { uri.getQueryParameter("code") } returns "c"
        every { uri.getQueryParameter("state") } returns "custom-state"
        val vm = viewModel()
        val nav = async { vm.navigateAfterAuth.first() }
        yield()
        vm.handleGoogleOAuthUri(uri)
        advanceUntilIdle()
        assertEquals(AuthViewModel.ROUTE_DASHBOARD, nav.await())
        assertTrue(vm.authState.value is AuthState.Authenticated)
    }

    @Test
    fun `oauth with access_denied error`() = runTest {
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns "access_denied"
        every { uri.getQueryParameter("error_description") } returns "User denied access"
        every { uri.getQueryParameter("code") } returns null
        val vm = viewModel()
        vm.handleGoogleOAuthUri(uri)
        advanceUntilIdle()

        assertEquals("User denied access", vm.oauthError.value)
    }

    @Test
    fun `oauth with server_error`() = runTest {
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns "server_error"
        every { uri.getQueryParameter("error_description") } returns "Internal server error"
        every { uri.getQueryParameter("code") } returns null
        val vm = viewModel()
        vm.handleGoogleOAuthUri(uri)
        advanceUntilIdle()

        assertEquals("Internal server error", vm.oauthError.value)
    }

    @Test
    fun `oauth with invalid state`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { loginUseCase.loginWithGoogle(any(), any()) } returns Result.failure(RuntimeException("Invalid state"))
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns null
        every { uri.getQueryParameter("code") } returns "c"
        every { uri.getQueryParameter("state") } returns "invalid-state"
        val vm = viewModel()
        vm.handleGoogleOAuthUri(uri)
        advanceUntilIdle()

        assertEquals("Invalid state", vm.oauthError.value)
    }

    @Test
    fun `session expiration notifier clears session`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = viewModel()
        sessionNotifier.notifySessionExpired()
        advanceUntilIdle()

        assertTrue(vm.authState.value is AuthState.Unauthenticated)
        assertNull(vm.currentUser.value)
    }

    @Test
    fun `multiple concurrent login attempts`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.success(TestFixtures.domainUser())
        val vm = viewModel()

        vm.login("test1@test.com", "password1")
        vm.login("test2@test.com", "password2")
        advanceUntilIdle()

        assertTrue(vm.authState.value is AuthState.Authenticated)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `login after successful registration`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { registerUseCase(any(), any(), any(), any(), any()) } returns Result.success(TestFixtures.domainUser())
        coEvery { loginUseCase(any(), any()) } returns Result.success(TestFixtures.domainUser())
        val vm = viewModel()

        vm.register("test@test.com", "password", "testuser", "First", "Last")
        advanceUntilIdle()
        assertTrue(vm.authState.value is AuthState.Authenticated)

        vm.login("test@test.com", "password")
        advanceUntilIdle()
        assertTrue(vm.authState.value is AuthState.Authenticated)
        assertNull(vm.errorMessage.value)
    }

    @Test
    fun `login failure without message uses default`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(Exception())
        val vm = viewModel()
        vm.login("a", "b")
        advanceUntilIdle()
        assertTrue(vm.authState.value is AuthState.Error)
        assertEquals("Login failed", (vm.authState.value as AuthState.Error).message)
        assertEquals("Login failed", vm.errorMessage.value)
    }

    @Test
    fun `oauth google failure without message uses default`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { loginUseCase.loginWithGoogle(any(), any()) } returns Result.failure(Exception())
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns null
        every { uri.getQueryParameter("code") } returns "c"
        every { uri.getQueryParameter("state") } returns null
        val vm = viewModel()
        vm.handleGoogleOAuthUri(uri)
        advanceUntilIdle()
        assertEquals("Error en login con Google", vm.oauthError.value)
        assertTrue(vm.authState.value is AuthState.Unauthenticated)
    }

    @Test
    fun `oauth error without description uses error code`() = runTest(mainDispatcherRule.dispatcher) {
        val uri = mockk<android.net.Uri>()
        every { uri.getQueryParameter("error") } returns "invalid_request"
        every { uri.getQueryParameter("error_description") } returns null
        every { uri.getQueryParameter("code") } returns null
        val vm = viewModel()
        vm.handleGoogleOAuthUri(uri)
        advanceUntilIdle()
        assertEquals("invalid_request", vm.oauthError.value)
    }
}
