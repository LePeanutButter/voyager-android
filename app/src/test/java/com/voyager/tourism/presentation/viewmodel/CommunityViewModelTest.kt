package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class CommunityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val socialRepo = mockk<SocialRepository>(relaxed = true)
    private val travelRepo = mockk<TravelRepository>(relaxed = true)
    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val supplementRepo = mockk<BackendSupplementRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>(relaxed = true)
    private lateinit var vm: CommunityViewModel

    @Before
    fun setup() {
        coEvery { socialRepo.getConnections(42L) } returns Result.success(emptyList())
        coEvery { socialRepo.getPendingRequests("") } returns emptyList()
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { travelRepo.getUserTravelPlans("42") } returns Result.success(emptyList())
        coEvery { socialRepo.getCompatibleTravelers(any(), any()) } returns emptyList()
        coEvery {
            voyagerAi.getTravelBuddyRecommendations(any(), any(), any(), any())
        } returns Response.success("{}".toResponseBody("application/json".toMediaType()))
        vm = CommunityViewModel(socialRepo, travelRepo, voyagerAi, supplementRepo, prefs)
    }

    @Test
    fun `initial ui state uses defaults`() {
        val fresh = CommunityViewModel(socialRepo, travelRepo, voyagerAi, supplementRepo, prefs)
        assertEquals(CommunityTab.CONNECTIONS, fresh.uiState.value.activeTab)
        assertEquals(null, fresh.uiState.value.error)
    }

    @Test
    fun `selectTab updates active tab`() {
        vm.selectTab(CommunityTab.REQUESTS)
        assertEquals(CommunityTab.REQUESTS, vm.uiState.value.activeTab)
    }

    @Test
    fun `loadInitial selects connections tab`() = runTest {
        vm.loadInitial()
        advanceUntilIdle()
        assertEquals(CommunityTab.CONNECTIONS, vm.uiState.value.activeTab)
    }

    @Test
    fun `clearError and clearInfoMessage`() {
        vm.clearError()
        vm.clearInfoMessage()
        assertEquals(null, vm.uiState.value.error)
        assertEquals(null, vm.uiState.value.infoMessage)
    }

    @Test
    fun `connections tab without user shows error`() = runTest {
        every { prefs.getCurrentUserId() } returns null
        vm.selectTab(CommunityTab.CONNECTIONS)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.error?.contains("sesión") == true)
    }

    @Test
    fun `sendDiscoverConnect success shows info`() = runTest {
        coEvery { socialRepo.sendConnectionRequest(any(), any()) } returns TestFixtures.connectionRequest()
        vm.sendDiscoverConnect(99L)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.infoMessage?.contains("Solicitud") == true)
        coVerify(atLeast = 1) { socialRepo.sendConnectionRequest(any(), any()) }
    }

    @Test
    fun `removeConnection success shows info`() = runTest {
        coEvery { supplementRepo.removeConnection(1L) } returns TestFixtures.apiResponse(200)
        vm.removeConnection(1L)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.infoMessage?.contains("eliminada") == true)
    }

    @Test
    fun `removeConnection non200 shows error`() = runTest {
        coEvery { supplementRepo.removeConnection(2L) } returns TestFixtures.apiResponse(400, message = "")
        vm.removeConnection(2L)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun `discover tab merges backend and ai rows`() = runTest {
        coEvery { travelRepo.getUserTravelPlans("42") } returns Result.success(
            listOf(TestFixtures.travelPlanDto(id = 1L)),
        )
        coEvery { socialRepo.getCompatibleTravelers("1", "") } returns listOf(TestFixtures.travelerMatch(10L))
        coEvery {
            voyagerAi.getTravelBuddyRecommendations(any(), any(), any(), any())
        } returns Response.success(
            """
            {"data":{"recommendations":[
              {"user_id":11,"name":"AI Buddy","compatibility_score":0.9,"shared_destinations":["Lima"]}
            ]}}
            """.trimIndent().toResponseBody("application/json".toMediaType()),
        )

        vm.selectTab(CommunityTab.DISCOVER)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.discoverRows.isNotEmpty())
        assertTrue(vm.uiState.value.aiHighlightRows.isNotEmpty())
    }

    @Test
    fun `rejectRequest failure sets error`() = runTest {
        coEvery { socialRepo.rejectConnectionRequest(any(), any()) } throws RuntimeException("fail")
        vm.rejectRequest(5L)
        advanceUntilIdle()
        assertEquals("fail", vm.uiState.value.error)
    }
}
