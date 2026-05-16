package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.AiTravelerMatchDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.DispatcherProvider
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestDispatcherProvider
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class CommunityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val socialRepo = mockk<SocialRepository>(relaxed = true)
    private val travelRepo = mockk<TravelRepository>(relaxed = true)
    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val supplementRepo = mockk<BackendSupplementRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>(relaxed = true)
    private val testDispatchers = TestDispatcherProvider(mainDispatcherRule.dispatcher)
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
        } returns Response.success(AiMatchingResponseDto(emptyList(), "42", 0))
        vm = CommunityViewModel(socialRepo, travelRepo, voyagerAi, supplementRepo, prefs, testDispatchers)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `initial ui state uses defaults`() {
        val fresh = CommunityViewModel(socialRepo, travelRepo, voyagerAi, supplementRepo, prefs, testDispatchers)
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
        val uid = "42"
        val planId = 1L
        val planIdStr = planId.toString()
        val dest = "Lima"

        coEvery { travelRepo.getUserTravelPlans(uid) } returns Result.success(
            listOf(TestFixtures.travelPlanDto(id = planId, destination = dest)),
        )
        
        coEvery { socialRepo.getCompatibleTravelers(planIdStr, "") } returns listOf(TestFixtures.travelerMatch(10L))
        
        coEvery {
            voyagerAi.getTravelBuddyRecommendations(
                userId = any(),
                location = any(),
                limit = any(),
                seekerFootprint = any()
            )
        } returns Response.success(
            AiMatchingResponseDto(
                matches = listOf(
                    AiTravelerMatchDto(
                        userId = "11",
                        name = "AI Buddy",
                        compatibilityScore = 95.0,
                        travelStyleMatch = 0.9,
                        sharedDestinations = listOf(dest)
                    )
                ),
                userId = uid,
                totalMatches = 1
            )
        )

        vm.selectTab(CommunityTab.DISCOVER)
        advanceUntilIdle()

        val state = vm.uiState.value
        val ids = state.discoverRows.map { it.userId }
        
        assertTrue("Discover rows should not be empty", state.discoverRows.isNotEmpty())
        assertTrue("Should contain backend match 10. Found: $ids", ids.contains(10L))
        assertTrue("Should contain AI match 11. Found: $ids", ids.contains(11L))
    }

    @Test
    fun `rejectRequest failure sets error`() = runTest {
        coEvery { socialRepo.rejectConnectionRequest(any(), any()) } throws RuntimeException("fail")
        vm.rejectRequest(5L)
        advanceUntilIdle()
        assertEquals("fail", vm.uiState.value.error)
    }

    @Test
    fun `setSelectedPlan refreshes discover with chosen plan id`() = runTest {
        val p1 = TestFixtures.travelPlanDto(id = 1L)
        val p2 = TestFixtures.travelPlanDto(id = 2L)
        coEvery { travelRepo.getUserTravelPlans("42") } returns Result.success(listOf(p1, p2))
        coEvery { socialRepo.getCompatibleTravelers("2", "") } returns emptyList()
        coEvery {
            voyagerAi.getTravelBuddyRecommendations(any(), any(), any(), any())
        } returns Response.success(AiMatchingResponseDto(emptyList(), "42", 0))

        vm.selectTab(CommunityTab.DISCOVER)
        advanceUntilIdle()
        vm.setSelectedPlan("2")
        advanceUntilIdle()

        coVerify(atLeast = 1) { socialRepo.getCompatibleTravelers("2", "") }
    }
}
