package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.model.CompatibilityMatch
import com.voyager.tourism.domain.model.SharedActivity
import com.voyager.tourism.domain.model.SharedActivityDecision
import com.voyager.tourism.domain.model.TravelActivity
import com.voyager.tourism.domain.model.TravelerConnection
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SocialCollaborationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<SocialRepository>()
    private lateinit var vm: SocialCollaborationViewModel

    @Before
    fun setup() {
        vm = SocialCollaborationViewModel(repository)
    }

    @Test
    fun `loadConnections success updates state`() = runTest {
        val list = listOf(TravelerConnection(1L, 2L, "u", "F", "L", "S"))
        coEvery { repository.getConnections(1L) } returns Result.success(list)

        vm.loadConnections(1L)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoadingConnections)
        assertEquals(list, vm.uiState.value.connections)
    }

    @Test
    fun `loadActivities success updates state`() = runTest {
        val list = listOf(TravelActivity(1L, "Act"))
        coEvery { repository.getTravelPlanActivities(10L) } returns Result.success(list)

        vm.loadActivities(10L)
        advanceUntilIdle()

        assertEquals(list, vm.uiState.value.activities)
    }

    @Test
    fun `shareActivity success updates state`() = runTest {
        val shared = SharedActivity(1L, 2L, 3L, 4L, "PENDING", false)
        coEvery { repository.shareActivity(2L, 4L) } returns Result.success(shared)

        vm.shareActivity(2L, 4L)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.sharedActivities.size)
        assertEquals("Activity shared successfully", vm.uiState.value.successMessage)
    }

    @Test
    fun `resolveSharedActivity updates status`() = runTest {
        val updated = SharedActivity(1L, 2L, 3L, 4L, "ACCEPTED", false)
        coEvery { repository.resolveSharedActivity(1L, SharedActivityDecision.ACCEPT) } returns Result.success(updated)

        vm.resolveSharedActivity(1L, SharedActivityDecision.ACCEPT)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.sharedActivities.any { it.status == "ACCEPTED" })
    }

    @Test
    fun `loadCompatibilityMatches filters by interest`() = runTest {
        val matches = listOf(
            CompatibilityMatch(1L, 1.0, 1.0, 1.0, 1.0, listOf("art")),
            CompatibilityMatch(2L, 0.5, 0.5, 0.5, 0.5, listOf("sport"))
        )
        coEvery { repository.getCompatibilityMatches(any(), any(), any(), any()) } returns Result.success(matches)

        vm.loadCompatibilityMatches("D", "S", "E", emptyList())
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.filteredMatches.size)

        vm.applyInterestsFilter(setOf("art"))
        assertEquals(1, vm.uiState.value.filteredMatches.size)
        assertEquals(1L, vm.uiState.value.filteredMatches.first().userId)
    }

    @Test
    fun `clearFeedback resets messages`() = runTest {
        val shared = SharedActivity(1L, 2L, 3L, 4L, "PENDING", false)
        coEvery { repository.shareActivity(any(), any()) } returns Result.success(shared)
        vm.shareActivity(1, 2)
        advanceUntilIdle()

        vm.clearFeedback()
        assertEquals(null, vm.uiState.value.successMessage)
        assertEquals(null, vm.uiState.value.errorMessage)
    }
}
