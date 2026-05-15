package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.usecase.trip.CreateTravelPlanParams
import com.voyager.tourism.domain.usecase.trip.CreateTravelPlanUseCase
import com.voyager.tourism.util.MainDispatcherRule
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class CreateTravelPlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCase = mockk<CreateTravelPlanUseCase>()
    private lateinit var vm: CreateTravelPlanViewModel

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        vm = CreateTravelPlanViewModel(useCase)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
    }


    @Test
    fun `initial state is Idle`() {
        assertTrue(vm.uiState.value is CreateTravelPlanUiState.Idle)
    }

    @Test
    fun `createPlan success emits Success`() = runTest {
        val plan = TestFixtures.travelPlanDto()
        coEvery { useCase(any()) } returns Result.success(plan)

        vm.createPlan("T", "D", "", "2027-01-10", "2027-01-20", 500_000.0, 2, "")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Success)
        assertEquals(plan, (state as CreateTravelPlanUiState.Success).travelPlan)
    }

    @Test
    fun `createPlan failure emits Error`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(IllegalArgumentException("bad"))

        vm.createPlan("T", "D", "O", "2027-02-01", "2027-02-10", 500_000.0, 1, "x")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Error)
        assertEquals("bad", (state as CreateTravelPlanUiState.Error).message)
    }

    @Test
    fun `createPlan with network error`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(RuntimeException("Network error"))

        vm.createPlan("T", "D", "O", "2027-02-01", "2027-02-10", 500_000.0, 1, "x")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Error)
        assertEquals("Network error", (state as CreateTravelPlanUiState.Error).message)
    }

    @Test
    fun `createPlan with validation error`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(IllegalArgumentException("Invalid dates"))

        vm.createPlan("T", "D", "O", "2027-02-01", "2027-01-10", 500_000.0, 1, "x")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Error)
        assertEquals("Invalid dates", (state as CreateTravelPlanUiState.Error).message)
    }

    @Test
    fun `resetState returns Idle`() {
        vm.resetState()
        assertTrue(vm.uiState.value is CreateTravelPlanUiState.Idle)
    }

    @Test
    fun `resetState clears previous error`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(RuntimeException("Previous error"))
        vm.createPlan("T", "D", "O", "2027-02-01", "2027-02-10", 500_000.0, 1, "")

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
            vm.validateDateRange("2027-02-10", "2027-01-01"),
        )
    }

    @Test
    fun `validateDateRange unparseable dates returns null`() {
        assertNull(vm.validateDateRange("not-a-date", "2027-01-01"))
        assertNull(vm.validateDateRange("2027-01-01", "not-a-date"))
    }

    @Test
    fun `validateDateRange same dates returns null`() {
        assertNull(vm.validateDateRange("2027-01-01", "2027-01-01"))
    }

    @Test
    fun `validateDateRange valid range returns null`() {
        assertNull(vm.validateDateRange("2027-01-01", "2027-01-10"))
        assertNull(vm.validateDateRange("2027-01-01", "2027-12-31"))
    }

    @Test
    fun `multiple createPlan calls work correctly`() = runTest {
        val plan1 = TestFixtures.travelPlanDto()
        val plan2 = TestFixtures.travelPlanDto()
        coEvery { useCase(any()) } returnsMany listOf(Result.success(plan1), Result.success(plan2))

        vm.createPlan("T", "D", "O", "2027-01-01", "2027-01-10", 500_000.0, 1, "")
        assertTrue(vm.uiState.value is CreateTravelPlanUiState.Success)
        assertEquals(plan1, (vm.uiState.value as CreateTravelPlanUiState.Success).travelPlan)

        vm.createPlan("T2", "D2", "O2", "2027-02-01", "2027-02-10", 600_000.0, 2, "")
        assertTrue(vm.uiState.value is CreateTravelPlanUiState.Success)
        assertEquals(plan2, (vm.uiState.value as CreateTravelPlanUiState.Success).travelPlan)
    }

    @Test
    fun `createPlan with empty title`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(RuntimeException("Title cannot be empty"))

        vm.createPlan("", "D", "O", "2027-01-01", "2027-01-10", 500_000.0, 1, "")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Error)
        assertEquals("Title cannot be empty", (state as CreateTravelPlanUiState.Error).message)
    }

    @Test
    fun `createPlan with negative budget`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(RuntimeException("Budget cannot be negative"))

        vm.createPlan("T", "D", "O", "2027-01-01", "2027-01-10", -1000.0, 1, "")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Error)
        assertEquals("Budget cannot be negative", (state as CreateTravelPlanUiState.Error).message)
    }

    @Test
    fun `createPlan with zero travelers`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(RuntimeException("Must have at least 1 traveler"))

        vm.createPlan("T", "D", "O", "2027-01-01", "2027-01-10", 500_000.0, 0, "")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Error)
        assertEquals("Must have at least 1 traveler", (state as CreateTravelPlanUiState.Error).message)
    }

    @Test
    fun `createPlan with very large budget`() = runTest {
        val plan = TestFixtures.travelPlanDto()
        coEvery { useCase(any()) } returns Result.success(plan)

        vm.createPlan("T", "D", "O", "2027-01-01", "2027-01-10", 10_000_000.0, 5, "")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Success)
        assertEquals(plan, (state as CreateTravelPlanUiState.Success).travelPlan)
    }

    @Test
    fun `createPlan with long description`() = runTest {
        val plan = TestFixtures.travelPlanDto()
        coEvery { useCase(any()) } returns Result.success(plan)

        val longDescription = "A".repeat(1000) // Very long description
        vm.createPlan("T", "D", "O", "2027-01-01", "2027-01-10", 500_000.0, 1, longDescription)

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Success)
        assertEquals(plan, (state as CreateTravelPlanUiState.Success).travelPlan)
    }

    @Test
    fun `createPlan with special characters in title`() = runTest {
        val plan = TestFixtures.travelPlanDto()
        coEvery { useCase(any()) } returns Result.success(plan)

        vm.createPlan("Título con ñ y áccents", "D", "O", "2027-01-01", "2027-01-10", 500_000.0, 1, "")

        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Success)
        assertEquals(plan, (state as CreateTravelPlanUiState.Success).travelPlan)
    }

    @Test
    fun `createPlan preserves iso datetime strings`() = runTest {
        val plan = TestFixtures.travelPlanDto()
        coEvery { useCase(any()) } returns Result.success(plan)
        vm.createPlan("T", "D", "O", "2027-03-01T08:00:00", "2027-03-10T18:00:00", 100.0, 1, "")
        coVerify {
            useCase(
                CreateTravelPlanParams(
                    title = "T",
                    destination = "D",
                    origin = "O",
                    startDate = "2027-03-01T00:00:00",
                    endDate = "2027-03-10T23:59:59",
                    budget = 100.0,
                    travelers = 1,
                    description = null,
                ),
            )
        }
    }

    @Test
    fun `createPlan failure without message uses default`() = runTest {
        coEvery { useCase(any()) } returns Result.failure(Exception())
        vm.createPlan("T", "D", "O", "2027-01-01", "2027-01-10", 1.0, 1, "")
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is CreateTravelPlanUiState.Error)
        assertEquals("Error desconocido", (state as CreateTravelPlanUiState.Error).message)
    }
}
