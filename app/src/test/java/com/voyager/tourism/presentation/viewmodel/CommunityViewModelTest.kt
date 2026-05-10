package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
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
        coEvery { prefs.getCurrentUserId() } returns "42"
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
}
