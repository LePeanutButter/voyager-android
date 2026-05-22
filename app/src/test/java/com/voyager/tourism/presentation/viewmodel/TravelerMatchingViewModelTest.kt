package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.usecase.social.GetCompatibleTravelersUseCase
import com.voyager.tourism.domain.usecase.social.SendConnectionRequestUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
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

    @After
    fun tearDown() {
    }

    @Test
    fun `findCompatibleTravelers success`() = runTest {
        val matches = listOf(TestFixtures.travelerMatch())
        coEvery { getCompatible("plan-9", "tok") } returns Result.success(matches)

        vm.findCompatibleTravelers("plan-9", "tok")
        advanceUntilIdle()

        assertEquals(matches, vm.uiState.value.compatibleTravelers)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `findCompatibleTravelers failure`() = runTest {
        coEvery { getCompatible(any(), any()) } returns Result.failure(RuntimeException("x"))

        vm.findCompatibleTravelers("p", "t")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.error!!.contains("x"))
    }

    @Test
    fun `sendConnectionRequest success refreshes list`() = runTest {
        vm.setCurrentTravelPlanId("plan-1")
        coEvery { sendRequest(5L, "hi", "tok") } returns Result.success(TestFixtures.connectionRequest())
        coEvery { getCompatible("plan-1", "tok") } returns Result.success(emptyList())

        vm.sendConnectionRequest(5L, "hi", "tok")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.successMessage!!.contains("sent", ignoreCase = true))
        assertEquals(false, vm.uiState.value.isSendingRequest)
    }

    @Test
    fun `sendConnectionRequest failure`() = runTest {
        coEvery { sendRequest(any(), any(), any()) } returns Result.failure(IllegalStateException("dup"))

        vm.sendConnectionRequest(1L, null, "t")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.error!!.contains("dup"))
    }

    @Test
    fun `setCurrentTravelPlanId and clear helpers`() = runTest {
        vm.setCurrentTravelPlanId("abc")
        assertEquals("abc", vm.uiState.value.currentTravelPlanId)

        coEvery { getCompatible(any(), any()) } returns Result.failure(RuntimeException("e"))
        vm.findCompatibleTravelers("a", "b")
        advanceUntilIdle()
        vm.clearError()
        assertNull(vm.uiState.value.error)

        vm.setCurrentTravelPlanId("p")
        coEvery { sendRequest(any(), any(), any()) } returns Result.success(TestFixtures.connectionRequest())
        coEvery { getCompatible("p", "t") } returns Result.success(emptyList())
        vm.sendConnectionRequest(1L, null, "t")
        advanceUntilIdle()
        vm.clearSuccessMessage()
        assertNull(vm.uiState.value.successMessage)
    }
}
