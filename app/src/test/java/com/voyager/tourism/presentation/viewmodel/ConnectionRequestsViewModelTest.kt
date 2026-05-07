package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.usecase.social.GetPendingRequestsUseCase
import com.voyager.tourism.domain.usecase.social.RespondToConnectionRequestUseCase
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

class ConnectionRequestsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPending = mockk<GetPendingRequestsUseCase>()
    private val respond = mockk<RespondToConnectionRequestUseCase>()
    private lateinit var vm: ConnectionRequestsViewModel

    @Before
    fun setup() {
        vm = ConnectionRequestsViewModel(getPending, respond)
    }

    @Test
    fun `loadPendingRequests success`() {
        val list = listOf(TestFixtures.connectionRequest())
        coEvery { getPending("tok") } returns Result.success(list)

        vm.loadPendingRequests("tok")

        assertEquals(false, vm.uiState.value.isLoading)
        assertEquals(list, vm.uiState.value.pendingRequests)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `loadPendingRequests failure`() {
        coEvery { getPending(any()) } returns Result.failure(RuntimeException("err"))

        vm.loadPendingRequests("t")

        assertTrue(vm.uiState.value.error!!.contains("err"))
    }

    @Test
    fun `acceptRequest success refreshes list`() {
        coEvery { respond.acceptRequest("1", "tok") } returns Result.success(TestFixtures.connectionRequest())
        coEvery { getPending("tok") } returns Result.success(emptyList())

        vm.acceptRequest("1", "tok")

        assertEquals(false, vm.uiState.value.isProcessing)
        assertTrue(vm.uiState.value.successMessage!!.contains("accepted", ignoreCase = true))
    }

    @Test
    fun `acceptRequest failure`() {
        coEvery { respond.acceptRequest(any(), any()) } returns Result.failure(RuntimeException("nope"))

        vm.acceptRequest("1", "t")

        assertTrue(vm.uiState.value.error!!.contains("nope"))
    }

    @Test
    fun `rejectRequest success`() {
        coEvery { respond.rejectRequest("2", "tok") } returns Result.success(TestFixtures.connectionRequest())
        coEvery { getPending("tok") } returns Result.success(emptyList())

        vm.rejectRequest("2", "tok")

        assertTrue(vm.uiState.value.successMessage!!.contains("reject", ignoreCase = true))
    }

    @Test
    fun `clearError and clearSuccessMessage`() {
        coEvery { getPending(any()) } returns Result.failure(RuntimeException("e"))
        vm.loadPendingRequests("x")
        vm.clearError()
        assertNull(vm.uiState.value.error)

        coEvery { respond.acceptRequest(any(), any()) } returns Result.success(TestFixtures.connectionRequest())
        coEvery { getPending(any()) } returns Result.success(emptyList())
        vm.acceptRequest("1", "t")
        vm.clearSuccessMessage()
        assertNull(vm.uiState.value.successMessage)
    }
}
