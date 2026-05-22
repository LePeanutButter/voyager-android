package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.BackendSupplementRepository
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestDispatcherProvider
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

/**
 * Cubre [CommunityViewModel.refreshDiscoverManual] cuando el gate bloquea la petición
 * (cooldown / límite por ventana) y los temporizadores usan [StandardTestDispatcher] para no
 * bloquear ni dejar minutos de [delay] reales.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CommunityViewModelRefreshGateTest {

    private val std = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(std)

    private val socialRepo = mockk<SocialRepository>(relaxed = true)
    private val travelRepo = mockk<TravelRepository>(relaxed = true)
    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val supplementRepo = mockk<BackendSupplementRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>(relaxed = true)
    private val testDispatchers = TestDispatcherProvider(std)
    private lateinit var vm: CommunityViewModel

    @Before
    fun setup() {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { socialRepo.getConnections(42L) } returns Result.success(emptyList())
        coEvery { socialRepo.getPendingRequests("") } returns emptyList()
        coEvery { travelRepo.getUserTravelPlans("42") } returns Result.success(emptyList())
        coEvery { socialRepo.getCompatibleTravelers(any(), any()) } returns emptyList()
        coEvery {
            voyagerAi.getTravelBuddyRecommendations(any(), any(), any(), any())
        } returns Response.success(AiMatchingResponseDto(userId = "42"))
        vm = CommunityViewModel(socialRepo, travelRepo, voyagerAi, supplementRepo, prefs, testDispatchers)
    }

    @After
    fun tearDown() {
    }


    @Test
    fun `refreshDiscoverManual when rate limited shows notice`() = runTest(std) {
        val now = System.currentTimeMillis()
        val stamps = listOf(
            now - 59_000,
            now - 58_000,
            now - 57_000,
            now - 56_000,
            now - 55_000,
            now - 54_000,
        )
        putManualRefreshTimestamps(vm, stamps)
        vm.refreshDiscoverManual()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.refreshNotice.contains("límite"))
        advanceTimeBy(2_000)
        advanceUntilIdle()
    }

    @Test
    fun `refreshDiscoverManual when allowed clears notice and fetches`() = runTest(std) {
        coEvery { travelRepo.getUserTravelPlans("42") } returns Result.success(
            listOf(TestFixtures.travelPlanDto(id = 1L)),
        )
        vm.selectTab(CommunityTab.DISCOVER)
        advanceUntilIdle()

        vm.refreshDiscoverManual()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.refreshNotice.isEmpty())
        coVerify(atLeast = 1) { socialRepo.getCompatibleTravelers(any(), any()) }
        advanceTimeBy(61_000)
        advanceUntilIdle()
    }
}

private fun putManualRefreshTimestamps(vm: CommunityViewModel, stamps: List<Long>) {
    val field = CommunityViewModel::class.java.declaredFields.singleOrNull {
        java.util.List::class.java.isAssignableFrom(it.type)
    } ?: error("List-backed timestamp field not found on CommunityViewModel")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val list = field.get(vm) as java.util.ArrayList<Long>
    list.clear()
    list.addAll(stamps)
}
