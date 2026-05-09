package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.LocalChatResponseDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

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
    fun `empty message ignored`() {
        vm.sendMessage("   ")
        assertTrue(vm.messages.value.isEmpty())
    }

    @Test
    fun `no user shows error on send`() {
        coEvery { prefs.getCurrentUserId() } returns null
        vm.sendMessage("hola")
        assertNotNull(vm.error.value)
    }

    @Test
    fun `successful local chat appends bubbles`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "42"
        coEvery { prefs.getOrCreateLocalChatSessionId("42") } returns "sid-1"
        coEvery { repo.getTrendsDashboard() } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )
        coEvery { repo.getLocalChatHistory("sid-1", 50) } returns Response.success(
            """{"messages":[]}""".toResponseBody("application/json".toMediaType()),
        )
        vm.ensureInitialized()
        yield()
        yield()

        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(LocalChatResponseDto(reply = "Hola"))
        vm.sendMessage(" ping ")
        yield()

        assertTrue(vm.messages.value.any { it.text.contains("Hola") })
    }

    @Test
    fun `failed local chat adds error bubble`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "42"
        coEvery { prefs.getOrCreateLocalChatSessionId("42") } returns "sid-1"
        coEvery { repo.getTrendsDashboard() } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )
        coEvery { repo.getLocalChatHistory("sid-1", 50) } returns Response.success(
            """{"messages":[]}""".toResponseBody("application/json".toMediaType()),
        )
        vm.ensureInitialized()
        yield()
        yield()

        coEvery { repo.postLocalChatMessage(any()) } returns Response.error(
            500,
            "err".toResponseBody(null),
        )
        vm.sendMessage("x")
        yield()
        assertTrue(vm.messages.value.last().text.contains("Error"))
    }

    @Test
    fun `clearConversation rotates session`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "7"
        coEvery { prefs.rotateLocalChatSessionId("7") } returns "new-sid"
        vm.clearConversation()
        yield()
        coVerify { prefs.rotateLocalChatSessionId("7") }
        assertTrue(vm.messages.value.any { it.text.contains("Conversación nueva") })
    }

    @Test
    fun `clearError`() {
        vm.clearError()
        assertEquals(null, vm.error.value)
    }
}
