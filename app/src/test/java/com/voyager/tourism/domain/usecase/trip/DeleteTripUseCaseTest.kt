package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.domain.repository.TripRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteTripUseCaseTest {

    private val tripRepository = mockk<TripRepository>()
    private lateinit var useCase: DeleteTripUseCase

    @Before
    fun setup() {
        useCase = DeleteTripUseCase(tripRepository)
    }

    @Test
    fun `fails when id blank`() = runTest {
        val r = useCase("")
        assertTrue(r.isFailure)
    }

    @Test
    fun `delegates when id valid`() = runTest {
        coEvery { tripRepository.deleteTrip("x") } returns Result.success(Unit)

        val r = useCase("x")

        assertTrue(r.isSuccess)
    }
}
