package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.model.CompatibilityMatch
import com.voyager.tourism.domain.model.SharedActivity
import com.voyager.tourism.domain.model.SharedActivityDecision
import com.voyager.tourism.domain.model.TravelActivity
import com.voyager.tourism.domain.model.TravelerConnection
import com.voyager.tourism.domain.repository.SocialRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SocialCollaborationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<SocialRepository>()
    private lateinit var vm: SocialCollaborationViewModel

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        vm = SocialCollaborationViewModel(repository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
    }

    @Test
    fun `loadConnections success and failure`() = runTest {
        coEvery { repository.getConnections(1L) } returns Result.success(
            listOf(
                TravelerConnection(
                    connectionId = 1L,
                    peerUserId = 2L,
                    username = "u",
                    firstName = null,
                    lastName = null,
                    status = "ACTIVE",
                ),
            ),
        )
        vm.loadConnections(1L)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.connections.size)

        coEvery { repository.getConnections(2L) } returns Result.failure(RuntimeException("e"))
        vm.loadConnections(2L)
        advanceUntilIdle()
        assertEquals("e", vm.uiState.value.errorMessage)
    }

    @Test
    fun `loadActivities success and failure`() = runTest {
        coEvery { repository.getTravelPlanActivities(9L) } returns Result.success(
            listOf(TravelActivity(1L, "Tour")),
        )
        vm.loadActivities(9L)
        advanceUntilIdle()
        assertEquals("Tour", vm.uiState.value.activities.first().name)

        coEvery { repository.getTravelPlanActivities(any()) } returns Result.failure(RuntimeException("x"))
        vm.loadActivities(1L)
        advanceUntilIdle()
        assertEquals("x", vm.uiState.value.errorMessage)
    }

    @Test
    fun `shareActivity success and failure`() = runTest {
        val shared = SharedActivity(1L, 2L, 3L, 4L, "PENDING", false)
        coEvery { repository.shareActivity(1L, 4L) } returns Result.success(shared)
        vm.shareActivity(1L, 4L)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.successMessage!!.contains("shared", ignoreCase = true))

        coEvery { repository.shareActivity(any(), any()) } returns Result.failure(IllegalStateException("no"))
        vm.shareActivity(1L, 1L)
        advanceUntilIdle()
        assertEquals("no", vm.uiState.value.errorMessage)
    }

    @Test
    fun `resolveSharedActivity updates existing or prepends new`() = runTest {
        val existing = SharedActivity(10L, 1L, 1L, 2L, "PENDING", false)
        vm.trackSharedActivity(existing)
        val updated = SharedActivity(10L, 1L, 1L, 2L, "ACCEPTED", true)
        coEvery { repository.resolveSharedActivity(10L, SharedActivityDecision.ACCEPT) } returns Result.success(updated)
        vm.resolveSharedActivity(10L, SharedActivityDecision.ACCEPT)
        advanceUntilIdle()
        assertEquals("ACCEPTED", vm.uiState.value.sharedActivities.first().status)

        val fresh = SharedActivity(99L, 1L, 1L, 2L, "PENDING", false)
        coEvery { repository.resolveSharedActivity(99L, SharedActivityDecision.REJECT) } returns Result.success(fresh)
        vm.resolveSharedActivity(99L, SharedActivityDecision.REJECT)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.sharedActivities.any { it.id == 99L })

        coEvery { repository.resolveSharedActivity(any(), any()) } returns Result.failure(RuntimeException("bad"))
        vm.resolveSharedActivity(1L, SharedActivityDecision.ACCEPT)
        advanceUntilIdle()
        assertEquals("bad", vm.uiState.value.errorMessage)
    }

    @Test
    fun `loadCompatibilityMatches and applyInterestsFilter`() = runTest {
        val m1 = CompatibilityMatch(1L, 1.0, 1.0, 1.0, 1.0, listOf("art", "food"))
        val m2 = CompatibilityMatch(2L, 1.0, 1.0, 1.0, 1.0, listOf("sport"))
        coEvery { repository.getCompatibilityMatches("Lima", "a", "b", emptyList()) } returns Result.success(listOf(m1, m2))
        vm.loadCompatibilityMatches("Lima", "a", "b", emptyList())
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.allMatches.size)

        vm.applyInterestsFilter(setOf("art"))
        assertEquals(1, vm.uiState.value.filteredMatches.size)
        assertEquals(1L, vm.uiState.value.filteredMatches.first().userId)

        coEvery { repository.getCompatibilityMatches(any(), any(), any(), any()) } returns Result.failure(RuntimeException("z"))
        vm.loadCompatibilityMatches("x", "y", "z", listOf("a"))
        advanceUntilIdle()
        assertEquals("z", vm.uiState.value.errorMessage)
    }

    @Test
    fun `trackSharedActivity and clearFeedback`() {
        val s = SharedActivity(5L, 1L, 1L, 2L, "X", false)
        vm.trackSharedActivity(s)
        assertEquals(5L, vm.uiState.value.sharedActivities.first().id)
        vm.clearFeedback()
        assertNull(vm.uiState.value.errorMessage)
        assertNull(vm.uiState.value.successMessage)
    }
}
