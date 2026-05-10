package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.LocalChatResponseDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

/**
 * Pruebas de superficie pública del asistente IA (sin acoplar a miembros privados del ViewModel).
 */
@ExperimentalCoroutinesApi
class AiAssistantViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<VoyagerAiRepository>()
    private val prefs = mockk<PreferencesManager>()
    private lateinit var vm: AiAssistantViewModel

    @Before
    fun setup() {
        vm = AiAssistantViewModel(repo, prefs)
    }

    @Test
    fun `no user shows error on send`() {
        coEvery { prefs.getCurrentUserId() } returns null
        vm.sendMessage("hola")
        assertEquals("Inicia sesión para usar el asistente", vm.error.value)
    }

    @Test
    fun `clearError clears observable error`() {
        coEvery { prefs.getCurrentUserId() } returns null
        vm.sendMessage("x")
        assertEquals("Inicia sesión para usar el asistente", vm.error.value)
        vm.clearError()
        assertEquals(null, vm.error.value)
    }

    @Test
    fun `clearConversation rotates session when user present`() = runTest {
        coEvery { prefs.getCurrentUserId() } returns "7"
        coEvery { prefs.rotateLocalChatSessionId("7") } returns "new-sid"
        vm.clearConversation()
        advanceUntilIdle()
        coVerify { prefs.rotateLocalChatSessionId("7") }
        assertTrue(vm.messages.value.any { it.text.contains("Conversación nueva") })
    }

    @Test
    fun `ensureInitialized and sendMessage append assistant reply`() = runTest {
        coEvery { prefs.getCurrentUserId() } returns "42"
        coEvery { prefs.getOrCreateLocalChatSessionId("42") } returns "sid-1"
        coEvery { repo.getTrendsDashboard() } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )
        coEvery { repo.getLocalChatHistory("sid-1", 50) } returns Response.success(
            """{"messages":[]}""".toResponseBody("application/json".toMediaType()),
        )
        vm.ensureInitialized()
        advanceUntilIdle()

        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(LocalChatResponseDto(reply = "Hola"))
        vm.sendMessage(" ping ")
        advanceUntilIdle()

        assertTrue(vm.messages.value.any { it.text.contains("Hola") })
    }
}
