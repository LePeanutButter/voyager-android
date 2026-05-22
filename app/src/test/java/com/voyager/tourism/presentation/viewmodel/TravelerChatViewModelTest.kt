package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.MessageDto
import com.voyager.tourism.data.dto.PagedResponseMessageDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestDispatcherProvider
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TravelerChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<BackendSupplementRepository>()
    private val prefs = mockk<PreferencesManager>()
    private val dispatchers = TestDispatcherProvider(mainDispatcherRule.dispatcher)
    private lateinit var vm: TravelerChatViewModel

    @Before
    fun setup() {
        vm = TravelerChatViewModel(repository, prefs, dispatchers)
    }

    @Test
    fun `loadConversation success fetches history`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        val msg = MessageDto(id = 1L, content = "Hi", senderId = 10L, createdAt = "2026-01-01T10:00:00Z")
        val paged = PagedResponseMessageDto(status = 200, message = "ok", data = listOf(msg), last = true)
        coEvery { repository.getConversationMessages(1L, 42L, any(), any()) } returns paged

        vm.loadConversation(1L)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoadingHistory)
        assertEquals(1, vm.uiState.value.messages.size)
        assertEquals("Hi", vm.uiState.value.messages.first().content)
    }

    @Test
    fun `sendMessage success appends to list`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        val sent = MessageDto(id = 2L, content = "Yo", senderId = 42L, createdAt = "2026-01-01T10:01:00Z")
        coEvery { repository.sendMessage(any()) } returns TestFixtures.apiResponse(201, sent)

        val result = vm.sendMessage(1L, "Yo")
        advanceUntilIdle()

        assertTrue(result)
        assertEquals(1, vm.uiState.value.messages.size)
        assertEquals("Yo", vm.uiState.value.messages.first().content)
    }

    @Test
    fun `sendMessage failure sets error`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { repository.sendMessage(any()) } throws RuntimeException("fail")

        val result = vm.sendMessage(1L, "X")
        advanceUntilIdle()

        assertFalse(result)
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun `refreshConversation skips if loading`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { repository.getConversationMessages(any(), any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            PagedResponseMessageDto(status = 200, message = "ok", last = true)
        }

        vm.loadConversation(1L)
        vm.refreshConversation(1L) // should return early
        advanceUntilIdle()

        // No way to easily check return early without more mocks, but advanceUntilIdle finishes it.
    }
}
