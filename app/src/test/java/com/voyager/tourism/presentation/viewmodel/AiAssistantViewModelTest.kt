package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.AiTrendDashboardDto
import com.voyager.tourism.data.dto.LocalChatResponseDto
import com.voyager.tourism.data.dto.LocalChatHistoryResponseDto
import com.voyager.tourism.data.dto.LocalChatMessageDto
import com.voyager.tourism.data.dto.LocalRecommendationResponseDto
import com.voyager.tourism.data.dto.LocalRecommendationItemDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

/**
 * Pruebas de superficie pública del asistente IA (sin acoplar a miembros privados del ViewModel).
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AiAssistantViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val repo = mockk<VoyagerAiRepository>()
    private val prefs = mockk<PreferencesManager>()
    private val dispatchers = TestDispatcherProvider(testDispatcher)
    private lateinit var vm: AiAssistantViewModel

    @Before
    fun setup() {
        vm = AiAssistantViewModel(repo, prefs, dispatchers)
    }

    @After
    fun tearDown() {
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
            AiTrendDashboardDto(
                trendingDestinations = emptyList(),
                popularActivities = emptyList(),
                emergingSegments = emptyList(),
                lastUpdated = "now"
            )
        )
        coEvery { repo.getLocalChatHistoryTyped(any(), any()) } returns Response.success(
            emptyList()
        )
        vm.ensureInitialized()
        advanceUntilIdle()

        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(LocalChatResponseDto(reply = "Hola"))
        vm.sendMessage(" ping ")
        advanceUntilIdle()

        assertTrue(vm.messages.value.any { it.text.contains("Hola") })
    }

    @Test
    fun `ensureInitialized without user leaves messages empty`() = runTest {
        coEvery { prefs.getCurrentUserId() } returns null
        vm.ensureInitialized()
        advanceUntilIdle()
        assertTrue(vm.messages.value.isEmpty())
    }

    @Test
    fun `ensureInitialized history http error shows fallback welcome`() = runTest {
        coEvery { prefs.getCurrentUserId() } returns "9"
        coEvery { prefs.getOrCreateLocalChatSessionId("9") } returns "sid-x"
        coEvery { repo.getTrendsDashboard() } returns Response.success(
            AiTrendDashboardDto(
                trendingDestinations = emptyList(),
                popularActivities = emptyList(),
                emergingSegments = emptyList(),
                lastUpdated = "now"
            )
        )
        coEvery { repo.getLocalChatHistory(any(), any()) } returns Response.error(
            500,
            "".toResponseBody(null),
        )
        vm.ensureInitialized()
        advanceUntilIdle()
        assertTrue(vm.messages.value.any { it.text.contains("Voyager IA") })
    }

    @Test
    fun `ensureInitialized parses history messages`() = runTest {
        coEvery { prefs.getCurrentUserId() } returns "3"
        coEvery { prefs.getOrCreateLocalChatSessionId("3") } returns "sid-h"
        coEvery { repo.getTrendsDashboard() } returns Response.success(
            AiTrendDashboardDto(
                trendingDestinations = emptyList(),
                popularActivities = emptyList(),
                emergingSegments = emptyList(),
                lastUpdated = "now"
            )
        )
        coEvery { repo.getLocalChatHistoryTyped(any(), any()) } returns Response.success(
            listOf(
                LocalChatResponseDto(reply = "u", role = "user"),
                LocalChatResponseDto(reply = "a", role = "assistant")
            )
        )
        vm.ensureInitialized()
        advanceUntilIdle()
        // verify history loading
        assertEquals(2, vm.messages.value.size)
        assertEquals("u", vm.messages.value[0].text)
        assertEquals("a", vm.messages.value[1].text)
    }

    @Test
    fun `sendMessage http error surfaces in transcript`() = runTest {
        coEvery { prefs.getCurrentUserId() } returns "5"
        coEvery { prefs.getOrCreateLocalChatSessionId("5") } returns "sid-e"
        coEvery { repo.postLocalChatMessage(any()) } returns Response.error(
            500,
            "srv".toResponseBody("text/plain".toMediaType()),
        )
        vm.sendMessage("hola")
        advanceUntilIdle()
        assertNotNull(vm.error.value)
        assertTrue(vm.messages.value.last().text.contains("srv"))
    }


    @Test
    fun `sendMessage with ranking trigger appends suggestions`() = runTest {
        coEvery { prefs.getCurrentUserId() } returns "42"
        coEvery { prefs.getOrCreateLocalChatSessionId("42") } returns "sid-r"
        coEvery { repo.getTrendsDashboard() } returns Response.success(
            AiTrendDashboardDto(
                trendingDestinations = listOf(
                    com.voyager.tourism.data.dto.TrendItemDto("1", "Paris", 0.1, 0.9)
                ),
                popularActivities = emptyList(),
                emergingSegments = emptyList(),
                lastUpdated = "now"
            )
        )
        coEvery { repo.getLocalChatHistoryTyped(any(), any()) } returns Response.success(
            emptyList()
        )
        vm.ensureInitialized()
        advanceUntilIdle()

        coEvery { repo.postLocalChatMessage(any()) } returns Response.success(LocalChatResponseDto(reply = "ok"))
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(
            LocalRecommendationResponseDto(
                items = listOf(
                    LocalRecommendationItemDto(
                        id = "1",
                        name = "Museo",
                        category = "match",
                        score = 0.5
                    )
                )
            )
        )
        vm.sendMessage("quiero recomendaciones de museos")
        advanceUntilIdle()

        assertTrue(vm.messages.value.last().text.contains("Sugerencias"))
    }
}
