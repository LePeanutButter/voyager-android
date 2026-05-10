package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.repository.SocialRepository
import com.voyager.tourism.domain.model.TravelerConnection
import com.voyager.tourism.presentation.viewmodel.CommunityTab
import com.voyager.tourism.presentation.viewmodel.CommunityUiState
import com.voyager.tourism.presentation.viewmodel.DiscoverMatchRow
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class CommunityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val socialRepo = mockk<SocialRepository>()
    private val prefs = mockk<PreferencesManager>()
    private lateinit var vm: CommunityViewModel
    private val testScope = TestScope()

    @Before
    fun setup() {
        vm = CommunityViewModel(socialRepo, prefs)
    }

    @Test
    fun `initial state is loading`() {
        assertTrue(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.connections.isEmpty())
        assertTrue(vm.uiState.value.pendingRequests.isEmpty())
        assertTrue(vm.uiState.value.discoverRows.isEmpty())
        assertTrue(vm.uiState.value.aiHighlightRows.isEmpty())
        assertTrue(vm.uiState.value.myPlans.isEmpty())
        assertTrue(vm.uiState.value.error == null)
        assertTrue(vm.uiState.value.infoMessage == null)
    }

    @Test
    fun `loadInitial loads data`() = runBlocking {
        coEvery { socialRepo.getMyConnections() } returns emptyList()
        coEvery { socialRepo.getPendingRequests() } returns emptyList()
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { prefs.getMyTravelPlans() } returns listOf(
            TravelPlanDto(id = "1", title = "Plan 1", destinationLocation = "Paris"),
            TravelPlanDto(id = "2", title = "Plan 2", destinationLocation = "London")
        )

        vm.loadInitial()
        testScope.advanceUntilIdle()

        coVerify { socialRepo.getMyConnections() }
        coVerify { socialRepo.getPendingRequests() }
        coVerify { prefs.getCurrentUserId() }
        coVerify { prefs.getMyTravelPlans() }
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(2, vm.uiState.value.myPlans.size)
        assertEquals("Plan 1", vm.uiState.value.myPlans[0].title)
        assertEquals("Plan 2", vm.uiState.value.myPlans[1].title)
    }

    @Test
    fun `loadInitial handles error`() = runBlocking {
        coEvery { socialRepo.getMyConnections() } throws RuntimeException("Network error")
        coEvery { prefs.getCurrentUserId() } returns "user123"

        vm.loadInitial()
        testScope.advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
        assertTrue(vm.uiState.value.connections.isEmpty())
    }

    @Test
    fun `selectTab updates active tab`() {
        vm.selectTab(CommunityTab.CONNECTIONS)
        assertEquals(CommunityTab.CONNECTIONS, vm.uiState.value.activeTab)
        
        vm.selectTab(CommunityTab.REQUESTS)
        assertEquals(CommunityTab.REQUESTS, vm.uiState.value.activeTab)
        
        vm.selectTab(CommunityTab.DISCOVER)
        assertEquals(CommunityTab.DISCOVER, vm.uiState.value.activeTab)
    }

    @Test
    fun `acceptRequest calls repository`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.acceptConnectionRequest(any(), any()) } returns Result.success(Unit)

        vm.acceptRequest(123L)
        testScope.advanceUntilIdle()

        coVerify { socialRepo.acceptConnectionRequest(123L, "user123") }
        coVerify(exactly = 1) { socialRepo.getPendingRequests() }
    }

    @Test
    fun `acceptRequest handles error`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.acceptConnectionRequest(any(), any()) } returns Result.failure(RuntimeException("Network error"))

        vm.acceptRequest(123L)
        testScope.advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
        coVerify(exactly = 0) { socialRepo.getPendingRequests() }
    }

    @Test
    fun `rejectRequest calls repository`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.rejectConnectionRequest(any(), any()) } returns Result.success(Unit)

        vm.rejectRequest(456L)
        testScope.advanceUntilIdle()

        coVerify { socialRepo.rejectConnectionRequest(456L, "user123") }
        coVerify(exactly = 1) { socialRepo.getPendingRequests() }
    }

    @Test
    fun `rejectRequest handles error`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.rejectConnectionRequest(any(), any()) } returns Result.failure(RuntimeException("Network error"))

        vm.rejectRequest(456L)
        testScope.advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
        coVerify(exactly = 0) { socialRepo.getPendingRequests() }
    }

    @Test
    fun `removeConnection calls repository`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.removeConnection(any()) } returns Result.success(Unit)

        vm.removeConnection(789L)
        testScope.advanceUntilIdle()

        coVerify { socialRepo.removeConnection(789L) }
        coVerify(exactly = 1) { socialRepo.getMyConnections() }
    }

    @Test
    fun `removeConnection handles error`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.removeConnection(any()) } returns Result.failure(RuntimeException("Network error"))

        vm.removeConnection(789L)
        testScope.advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
        coVerify(exactly = 0) { socialRepo.getMyConnections() }
    }

    @Test
    fun `setSelectedPlan updates selected plan`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"

        vm.setSelectedPlan("plan-456")
        testScope.advanceUntilIdle()

        coVerify { prefs.setCurrentSelectedPlanId("plan-456") }
        assertEquals("plan-456", vm.uiState.value.selectedPlanId)
    }

    @Test
    fun `refreshDiscoverManual calls repository`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.refreshDiscover(any()) } returns Result.success(Unit)

        vm.refreshDiscoverManual()
        testScope.advanceUntilIdle()

        coVerify { socialRepo.refreshDiscover("user123") }
    }

    @Test
    fun `refreshDiscoverManual handles error`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.refreshDiscover(any()) } returns Result.failure(RuntimeException("Network error"))

        vm.refreshDiscoverManual()
        testScope.advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
    }

    @Test
    fun `sendDiscoverConnect calls repository`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.sendDiscoverConnectRequest(any(), any()) } returns Result.success(Unit)

        vm.sendDiscoverConnect(987L)
        testScope.advanceUntilIdle()

        coVerify { socialRepo.sendDiscoverConnectRequest(987L, "user123") }
    }

    @Test
    fun `sendDiscoverConnect handles error`() = runBlocking {
        coEvery { prefs.getCurrentUserId() } returns "user123"
        coEvery { socialRepo.sendDiscoverConnectRequest(any(), any()) } returns Result.failure(RuntimeException("Network error"))

        vm.sendDiscoverConnect(987L)
        testScope.advanceUntilIdle()

        assertEquals("Network error", vm.uiState.value.error)
    }

    @Test
    fun `clearInfoMessage clears info message`() {
        vm._uiState.value = CommunityUiState(
            isLoading = false,
            connections = emptyList(),
            pendingRequests = emptyList(),
            discoverRows = emptyList(),
            aiHighlightRows = emptyList(),
            myPlans = emptyList(),
            error = null,
            infoMessage = "Test info message"
        )
        
        vm.clearInfoMessage()
        
        assertEquals(null, vm.uiState.value.infoMessage)
    }

    @Test
    fun `clearError clears error state`() {
        vm._uiState.value = CommunityUiState(
            isLoading = false,
            connections = emptyList(),
            pendingRequests = emptyList(),
            discoverRows = emptyList(),
            aiHighlightRows = emptyList(),
            myPlans = emptyList(),
            error = "Test error",
            infoMessage = null
        )
        
        vm.clearError()
        
        assertEquals(null, vm.uiState.value.error)
    }

    @Test
    fun `parseBuddyRecommendations with valid input`() = runBlocking {
        val inputJson = """{"recommendations": [{"name": "Paris", "type": "city"}, {"name": "Beach", "type": "activity"}]}"""
        
        val result = vm.parseBuddyRecommendations(inputJson)
        
        assertEquals(2, result.size)
        assertEquals("Paris", result[0].name)
        assertEquals("city", result[0].type)
        assertEquals("Beach", result[1].name)
        assertEquals("activity", result[1].type)
    }

    @Test
    fun `parseBuddyRecommendations with invalid input`() = runBlocking {
        val inputJson = """{"invalid": "json"}"""
        
        val result = vm.parseBuddyRecommendations(inputJson)
        
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mergeDiscoveryMatches with empty lists`() = runBlocking {
        val compat = emptyList<TravelerConnection>()
        val buddyJson = """{"recommendations": []}"""
        val currentUserId = 123L
        
        val result = vm.mergeDiscoveryMatches(compat, buddyJson, currentUserId)
        
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mergeDiscoveryMatches with data`() = runBlocking {
        val compat = listOf(
            TravelerConnection(
                connectionId = 1L,
                userId = 111L,
                username = "user1",
                firstName = "John",
                lastName = "Doe",
                status = "connected"
            ),
            TravelerConnection(
                connectionId = 2L,
                userId = 222L,
                username = "user2", 
                firstName = "Jane",
                lastName = "Smith",
                status = "connected"
            )
        )
        val buddyJson = """{"recommendations": [{"name": "Paris", "type": "city"}, {"name": "Beach", "type": "activity"}]}"""
        val currentUserId = 123L
        
        val result = vm.mergeDiscoveryMatches(compat, buddyJson, currentUserId)
        
        assertEquals(2, result.size)
        assertEquals(111L, result[0].userId)
        assertEquals("user1", result[0].username)
        assertEquals("Paris", result[0].destinationLocation)
        assertEquals("city", result[0].type)
        assertEquals(222L, result[1].userId)
        assertEquals("user2", result[1].username)
        assertEquals("Beach", result[1].destinationLocation)
        assertEquals("activity", result[1].type)
    }

    @Test
    fun `mergeDiscoveryMatches excludes current user`() = runBlocking {
        val compat = listOf(
            TravelerConnection(
                connectionId = 1L,
                userId = 111L,
                username = "user1",
                firstName = "John",
                lastName = "Doe",
                status = "connected"
            )
        )
        val buddyJson = """{"recommendations": [{"name": "Paris", "type": "city"}, {"name": "Beach", "type": "activity"}]}"""
        val currentUserId = 111L
        
        val result = vm.mergeDiscoveryMatches(compat, buddyJson, currentUserId)
        
        assertEquals(1, result.size) // Should exclude current user
    }

    @Test
    fun `discoverCooldown calculation works`() {
        // Test with recent refresh
        vm._uiState.value = CommunityUiState(
            isLoading = false,
            connections = emptyList(),
            pendingRequests = emptyList(),
            discoverRows = emptyList(),
            aiHighlightRows = emptyList(),
            myPlans = emptyList(),
            error = null,
            infoMessage = null,
            refreshCooldownSec = 0,
            lastDiscoverRefresh = LocalDateTime.now().minusSeconds(30)
        )
        
        assertEquals(30, vm.discoverCooldownSec)
        
        // Test with no recent refresh
        vm._uiState.value = vm.uiState.value.copy(lastDiscoverRefresh = null)
        
        assertEquals(60, vm.discoverCooldownSec)
        
        // Test with refresh 45 seconds ago
        vm._uiState.value = vm.uiState.value.copy(lastDiscoverRefresh = LocalDateTime.now().minusSeconds(45))
        
        assertEquals(15, vm.discoverCooldownSec)
    }

    @Test
    fun `discoverNotice generation`() {
        // Test cooldown active
        vm._uiState.value = CommunityUiState(
            isLoading = false,
            connections = emptyList(),
            pendingRequests = emptyList(),
            discoverRows = emptyList(),
            aiHighlightRows = emptyList(),
            myPlans = emptyList(),
            error = null,
            infoMessage = null,
            refreshCooldownSec = 15,
            lastDiscoverRefresh = LocalDateTime.now().minusSeconds(45)
        )
        
        assertTrue(vm.discoverNotice.value.contains("Puedes refrescar en 15 segundos"))
        
        // Test cooldown not active
        vm._uiState.value = vm.uiState.value.copy(refreshCooldownSec = 0)
        
        assertTrue(vm.discoverNotice.value.contains("Refrescar descubrimiento"))
    }
}
