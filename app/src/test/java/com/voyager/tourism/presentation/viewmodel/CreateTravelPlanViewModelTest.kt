package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.usecase.trip.CreateTravelPlanUseCase
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateTravelPlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = mockk<CreateTravelPlanUseCase>()
    private lateinit var vm: CreateTravelPlanViewModel

    @Before
    fun setup() {
        vm = CreateTravelPlanViewModel(useCase)
    }

    @Test
    fun `createPlan success emits Success`() = runTest {
        val plan = TestFixtures.travelPlanDto()
        coEvery { useCase(any()) } returns Result.success(plan)

        vm.createPlan("T", "D", "", "2027-01-10", "2027-01-20", 100.0, 2, "")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Success)
        assertEquals(plan, (state as CreateTravelPlanUiState.Success).travelPlan)
    }

    @Test
    fun `createPlan failure emits Error`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(IllegalArgumentException("bad"))

        vm.createPlan("T", "D", "O", "2027-02-01", "2027-02-10", null, 1, "x")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Error)
        assertEquals("bad", (state as CreateTravelPlanUiState.Error).message)
    }

    @Test
    fun `resetState returns Idle`() {
        vm.resetState()
        assertTrue(vm.uiState.value is CreateTravelPlanUiState.Idle)
    }

    @Test
    fun `validateDateRange blank dates returns null`() {
        assertNull(vm.validateDateRange("", "2027-01-01"))
        assertNull(vm.validateDateRange("2027-01-01", ""))
    }

    @Test
    fun `validateDateRange end before start returns message`() {
        assertEquals(
            "El rango de fechas es inválido",
            vm.validateDateRange("2027-02-10", "2027-02-01"),
        )
    }

    @Test
    fun `validateDateRange unparseable dates returns null`() {
        assertNull(vm.validateDateRange("not-a-date", "2027-01-01"))
    }
}
