package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanRequest
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateTravelPlanUseCaseTest {

    private val travelRepository = mockk<TravelRepository>()
    private lateinit var useCase: CreateTravelPlanUseCase

    @Before
    fun setup() {
        useCase = CreateTravelPlanUseCase(travelRepository)
    }

    private fun futureRange(): Pair<String, String> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val start = fmt.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 3)
        val end = fmt.format(cal.time)
        return start to end
    }

    private fun baseParams(start: String, end: String) = CreateTravelPlanParams(
        title = "Paris",
        destination = "CDG",
        origin = "MAD",
        startDate = start,
        endDate = end,
        budget = 100.0,
        travelers = 2,
        description = "  notes  ",
    )

    @Test
    fun `fails when title blank`() = runTest {
        val (s, e) = futureRange()
        val r = useCase(
            baseParams(s, e).copy(title = "   "),
        )
        assertTrue(r.isFailure)
        assertEquals("El título es requerido", r.exceptionOrNull()?.message)
    }

    @Test
    fun `fails when destination blank`() = runTest {
        val (s, e) = futureRange()
        val r = useCase(baseParams(s, e).copy(destination = ""))
        assertTrue(r.isFailure)
        assertEquals("El destino es requerido", r.exceptionOrNull()?.message)
    }

    @Test
    fun `fails on invalid start date`() = runTest {
        val (_, e) = futureRange()
        val r = useCase(baseParams("not-a-date", e))
        assertTrue(r.isFailure)
        assertTrue(r.exceptionOrNull()?.message?.contains("inicio") == true)
    }

    @Test
    fun `fails when end before start`() = runTest {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 5)
        val end = fmt.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val start = fmt.format(cal.time)
        val r = useCase(baseParams(start, end).copy(startDate = end, endDate = start))
        assertTrue(r.isFailure)
        assertEquals("El rango de fechas es inválido", r.exceptionOrNull()?.message)
    }

    @Test
    fun `fails when start in past`() = runTest {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -10)
        val past = fmt.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 20)
        val end = fmt.format(cal.time)
        val r = useCase(baseParams(past, end))
        assertTrue(r.isFailure)
        assertEquals("La fecha de inicio no puede ser en el pasado", r.exceptionOrNull()?.message)
    }

    @Test
    fun `fails when travelers below 1`() = runTest {
        val (s, e) = futureRange()
        val r = useCase(baseParams(s, e).copy(travelers = 0))
        assertTrue(r.isFailure)
    }

    @Test
    fun `fails when budget negative`() = runTest {
        val (s, e) = futureRange()
        val r = useCase(baseParams(s, e).copy(budget = -1.0))
        assertTrue(r.isFailure)
    }

    @Test
    fun `success builds trimmed request and calls repository`() = runTest {
        val (s, e) = futureRange()
        val plan = TestFixtures.travelPlanDto()
        val slot = slot<TravelPlanRequest>()
        coEvery { travelRepository.createTravelPlan(capture(slot)) } returns Result.success(plan)

        val r = useCase(
            baseParams(s, e).copy(
                title = "  T  ",
                destination = "  Lima  ",
                origin = "  ",
                description = null,
            ),
        )

        assertTrue(r.isSuccess)
        coVerify(exactly = 1) { travelRepository.createTravelPlan(any()) }
        val req = slot.captured
        assertEquals("T", req.title)
        assertEquals("Lima", req.destinationLocation)
        assertEquals(null, req.originLocation)
        assertEquals(null, req.description)
    }

    @Test
    fun `repository exception becomes failure`() = runTest {
        val (s, e) = futureRange()
        coEvery { travelRepository.createTravelPlan(any()) } throws IllegalStateException("db")
        val r = useCase(baseParams(s, e))
        assertTrue(r.isFailure)
    }
}
