package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.AiChatReplyDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import okhttp3.ResponseBody.Companion.toResponseBody
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
    fun `no user shows error`() {
        coEvery { prefs.getCurrentUserId() } returns null
        vm.sendMessage("hola")
        assertNotNull(vm.error.value)
    }

    @Test
    fun `successful chat appends bubbles`() {
        coEvery { prefs.getCurrentUserId() } returns "42"
        coEvery { repo.postChat(any()) } returns Response.success(AiChatReplyDto("Hola"))
        vm.sendMessage(" ping ")
        assertEquals(2, vm.messages.value.size)
        assertTrue(vm.messages.value.last().text.contains("Hola"))
    }

    @Test
    fun `failed http adds error bubble`() {
        coEvery { prefs.getCurrentUserId() } returns "42"
        coEvery { repo.postChat(any()) } returns Response.error(
            500,
            "err".toResponseBody(null),
        )
        vm.sendMessage("x")
        assertTrue(vm.messages.value.last().text.contains("Error"))
    }

    @Test
    fun `clearError`() {
        vm.clearError()
        assertEquals(null, vm.error.value)
    }
}
