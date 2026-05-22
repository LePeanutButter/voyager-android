package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.CatalogApiService
import com.voyager.tourism.data.api.FlightOffersQuery
import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.FlightOfferDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogRepositoryImplTest {

    private fun <T> ok(data: T?) = ApiResponse("t", 200, "OK", data, null, null)

    @Test
    fun `delegates flightOffers to underlying api`() = runTest {
        val api = mockk<CatalogApiService>()
        val repo = CatalogRepositoryImpl(api)
        coEvery { api.flightOffers(any()) } returns ok(emptyList<FlightOfferDto>())
        val q = FlightOffersQuery("LIM", "CUZ", "2027-08-01", adults = 2)
        assertEquals(200, repo.flightOffers(q.toQueryMap()).status)
        coVerify { api.flightOffers(q.toQueryMap()) }
    }

    @Test
    fun `delegates other catalog calls`() = runTest {
        val api = mockk<CatalogApiService>()
        val repo = CatalogRepositoryImpl(api)
        coEvery { api.hotelsByCity("LIM") } returns ok(null)
        assertEquals(200, repo.hotelsByCity("LIM").status)
        
        coEvery { api.hotelOffers("h1", "2027-01-01", "2027-01-05", 1, 1, "USD") } returns ok(null)
        assertEquals(200, repo.hotelOffers("h1", "2027-01-01", "2027-01-05", 1, 1, "USD").status)
        
        coEvery { api.activities(1.0, 2.0, 10.0, "KM") } returns ok(emptyList())
        assertEquals(200, repo.activities(1.0, 2.0, 10.0, "KM").status)
    }
}
