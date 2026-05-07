package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.usecase.social.GetCompatibleTravelersUseCase
import com.voyager.tourism.domain.usecase.social.SendConnectionRequestUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TravelerMatchingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCompatible = mockk<GetCompatibleTravelersUseCase>()
    private val sendRequest = mockk<SendConnectionRequestUseCase>()
    private lateinit var vm: TravelerMatchingViewModel

    @Before
    fun setup() {
        vm = TravelerMatchingViewModel(getCompatible, sendRequest)
    }

    @Test
    fun `findCompatibleTravelers success`() {
        val matches = listOf(TestFixtures.travelerMatch())
        coEvery { getCompatible("plan-9", "tok") } returns Result.success(matches)

        vm.findCompatibleTravelers("plan-9", "tok")

        assertEquals(matches, vm.uiState.value.compatibleTravelers)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `findCompatibleTravelers failure`() {
        coEvery { getCompatible(any(), any()) } returns Result.failure(RuntimeException("x"))

        vm.findCompatibleTravelers("p", "t")

        assertTrue(vm.uiState.value.error!!.contains("x"))
    }

    @Test
    fun `sendConnectionRequest success refreshes list`() {
        vm.setCurrentTravelPlanId("plan-1")
        coEvery { sendRequest(5L, "hi", "tok") } returns Result.success(TestFixtures.connectionRequest())
        coEvery { getCompatible("plan-1", "tok") } returns Result.success(emptyList())

        vm.sendConnectionRequest(5L, "hi", "tok")

        assertTrue(vm.uiState.value.successMessage!!.contains("sent", ignoreCase = true))
        assertEquals(false, vm.uiState.value.isSendingRequest)
    }

    @Test
    fun `sendConnectionRequest failure`() {
        coEvery { sendRequest(any(), any(), any()) } returns Result.failure(IllegalStateException("dup"))

        vm.sendConnectionRequest(1L, null, "t")

        assertTrue(vm.uiState.value.error!!.contains("dup"))
    }

    @Test
    fun `setCurrentTravelPlanId and clear helpers`() {
        vm.setCurrentTravelPlanId("abc")
        assertEquals("abc", vm.uiState.value.currentTravelPlanId)

        coEvery { getCompatible(any(), any()) } returns Result.failure(RuntimeException("e"))
        vm.findCompatibleTravelers("a", "b")
        vm.clearError()
        assertNull(vm.uiState.value.error)

        vm.setCurrentTravelPlanId("p")
        coEvery { sendRequest(any(), any(), any()) } returns Result.success(TestFixtures.connectionRequest())
        coEvery { getCompatible("p", "t") } returns Result.success(emptyList())
        vm.sendConnectionRequest(1L, null, "t")
        vm.clearSuccessMessage()
        assertNull(vm.uiState.value.successMessage)
    }
}
