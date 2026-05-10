package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.LocalChatResponseDto
import com.voyager.tourism.data.dto.LocalRecommendationRequestDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.localai.LocalRecommendationParsers
import com.voyager.tourism.data.localai.LocalChatHistoryParsers
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class AiAssistantViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val repo = mockk<VoyagerAiRepository>()
    private val prefs = mockk<PreferencesManager>()
    private lateinit var vm: AiAssistantViewModel
    private val testScope = TestScope()

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
        testScope.advanceUntilIdle()

        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(LocalChatResponseDto(reply = "Hola"))
        vm.sendMessage(" ping ")
        testScope.advanceUntilIdle()

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
        testScope.advanceUntilIdle()

        coEvery { repo.postLocalChatMessage(any()) } returns Response.error(
            500,
            "err".toResponseBody(null),
        )
        vm.sendMessage("x")
        testScope.advanceUntilIdle()

        assertTrue(vm.messages.value.last().text.contains("Error"))
    }

    @Test
    fun `clearConversation rotates session`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "7"
        coEvery { prefs.rotateLocalChatSessionId("7") } returns "new-sid"
        vm.clearConversation()
        testScope.advanceUntilIdle()

        coVerify { prefs.rotateLocalChatSessionId("7") }
        assertTrue(vm.messages.value.any { it.text.contains("Conversación nueva") })
    }

    @Test
    fun `clearError clears error state`() {
        vm._error.value = "test error"
        vm.clearError()
        assertEquals(null, vm.error.value)
    }

    @Test
    fun `wantsLocalRanking returns true for trigger words`() {
        assertTrue(vm.wantsLocalRanking("ranking"))
        assertTrue(vm.wantsLocalRanking("sugerencias"))
        assertTrue(vm.wantsLocalRanking("recomendaciones"))
        assertFalse(vm.wantsLocalRanking("normal text"))
    }

    @Test
    fun `wantsLocalRanking returns false for non-trigger words`() {
        assertFalse(vm.wantsLocalRanking("normal conversation"))
        assertFalse(vm.wantsLocalRanking("just chatting"))
    }

    @Test
    fun `sendMessageAndGetReply success - no recommendations`() = runBlocking {
        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(
            LocalChatResponseDto(reply = "Basic reply")
        )
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(
            """{"items": []}""".toResponseBody("application/json".toMediaType())
        )

        val result = vm.sendMessageAndGetReply("user123", "session1", "test message")
        assertEquals("Basic reply", result)
    }

    @Test
    fun `sendMessageAndGetReply success - with recommendations`() = runBlocking {
        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(
            LocalChatResponseDto(reply = "Basic reply")
        )
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(
            """{"items": [{"name": "Paris"}, {"name": "London"}]}""".toResponseBody("application/json".toMediaType()),
        )

        val result = vm.sendMessageAndGetReply("user123", "session1", "test message")
        assertTrue(result.contains("Paris"))
        assertTrue(result.contains("London"))
    }

    @Test
    fun `sendMessageAndGetReply handles chat error`() = runBlocking {
        coEvery { repo.postLocalChatMessage(any()) } returns Response.error(
            500,
            "Server error".toResponseBody(null),
        )

        try {
            vm.sendMessageAndGetReply("user123", "session1", "test message")
            assertTrue(false) // Should not reach here
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Error") == true)
        }
    }

    @Test
    fun `sendMessageAndGetReply handles recommendation error`() = runBlocking {
        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(
            LocalChatResponseDto(reply = "Basic reply")
        )
        coEvery { repo.postLocalRecommendations(any()) } returns Response.error(
            500,
            "Recommendation error".toResponseBody(null),
        )

        val result = vm.sendMessageAndGetReply("user123", "session1", "test message")
        assertEquals("Basic reply", result) // Should return original reply when recommendation fails
    }

    @Test
    fun `sendMessageAndGetReply handles empty recommendation pool`() = runBlocking {
        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(
            LocalChatResponseDto(reply = "Basic reply")
        )
        
        // Set empty recommendation pool
        vm.recommendationPool = emptyList()

        val result = vm.sendMessageAndGetReply("user123", "session1", "test message")
        assertEquals("Basic reply", result) // Should not enrich when pool is empty
    }

    @Test
    fun `loadHistoryIntoMessages success`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { repo.getLocalChatHistory("session1", 50) } returns Response.success(
            """{"messages": [{"user": true, "text": "Hello"}, {"user": false, "text": "Hi"}]}""".toResponseBody("application/json".toMediaType()),
        )

        vm.loadHistoryIntoMessages("session1")
        testScope.advanceUntilIdle()

        assertEquals(2, vm.messages.value.size)
        assertTrue(vm.messages.value.any { it.isUser && it.text == "Hello" })
        assertTrue(vm.messages.value.any { !it.isUser && it.text == "Hi" })
    }

    @Test
    fun `loadHistoryIntoMessages failure`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { repo.getLocalChatHistory("session1", 50) } returns Response.error(
            500,
            "History error".toResponseBody(null),
        )

        vm.loadHistoryIntoMessages("session1")
        testScope.advanceUntilIdle()

        assertEquals(1, vm.messages.value.size)
        assertEquals(vm.welcomeFallback(), vm.messages.value.first().text)
    }

    @Test
    fun `loadHistoryIntoMessages empty history`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { repo.getLocalChatHistory("session1", 50) } returns Response.success(
            """{"messages": []}""".toResponseBody("application/json".toMediaType()),
        )

        vm.loadHistoryIntoMessages("session1")
        testScope.advanceUntilIdle()

        assertEquals(1, vm.messages.value.size)
        assertEquals(vm.welcomeEmptyHistory(), vm.messages.value.first().text)
    }

    @Test
    fun `refreshRecommendationPool success`() = runBlocking {
        val mockResponse = Response.success(
            """{"destinations": [{"name": "Paris", "country": "France"}, {"name": "London", "country": "UK"}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { repo.getTrendsDashboard() } returns mockResponse

        vm.refreshRecommendationPool()
        testScope.advanceUntilIdle()

        assertEquals(2, vm.recommendationPool.size)
        assertTrue(vm.recommendationPool.any { it.name == "Paris" })
        assertTrue(vm.recommendationPool.any { it.country == "France" })
    }

    @Test
    fun `refreshRecommendationPool failure`() = runBlocking {
        coEvery { repo.getTrendsDashboard() } returns Response.error(
            500,
            "Network error".toResponseBody(null),
        )

        vm.refreshRecommendationPool()
        testScope.advanceUntilIdle()

        assertTrue(vm.recommendationPool.isEmpty())
    }

    @Test
    fun `welcomeFallback returns fallback message`() {
        val message = vm.welcomeFallback()
        assertNotNull(message)
        assertTrue(message.contains("Hola"))
    }

    @Test
    fun `welcomeEmptyHistory returns empty history message`() {
        val message = vm.welcomeEmptyHistory()
        assertNotNull(message)
        assertTrue(message.contains("historial"))
    }

    @Test
    fun `welcomeNewSession returns new session message`() {
        val message = vm.welcomeNewSession()
        assertNotNull(message)
        assertTrue(message.contains("nueva"))
    }

    @Test
    fun `ensureInitialized loads history and recommendations`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { prefs.getOrCreateLocalChatSessionId("user123") } returns "session1"
        coEvery { repo.getLocalChatHistory("session1", 50) } returns Response.success(
            """{"messages": []}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { repo.getTrendsDashboard() } returns Response.success(
            """{"destinations": []}""".toResponseBody("application/json".toMediaType()),
        )

        vm.ensureInitialized()
        testScope.advanceUntilIdle()

        coVerify { repo.getLocalChatHistory("session1", 50) }
        coVerify { repo.getTrendsDashboard() }
        assertEquals(1, vm.messages.value.size) // Welcome message
    }

    @Test
    fun `ensureInitialized with blank userId does nothing`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns ""
        coEvery { repo.getLocalChatHistory(any(), any()) } returns Response.success(
            """{"messages": []}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { repo.getTrendsDashboard() } returns Response.success(
            """{"destinations": []}""".toResponseBody("application/json".toMediaType()),
        )

        vm.ensureInitialized()
        testScope.advanceUntilIdle()

        coVerify(exactly = 0) { repo.getLocalChatHistory(any(), any()) }
        coVerify(exactly = 0) { repo.getTrendsDashboard() }
        assertEquals(0, vm.messages.value.size)
    }
}
