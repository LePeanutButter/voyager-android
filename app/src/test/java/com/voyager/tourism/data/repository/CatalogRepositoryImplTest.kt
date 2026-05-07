package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.CatalogApiService
import com.voyager.tourism.data.api.FlightOffersQuery
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class CatalogRepositoryImplTest {

    @Test
    fun `delegates flightOffers to underlying api`() = runTest {
        val api = mockk<CatalogApiService>()
        val repo = CatalogRepositoryImpl(api)
        coEvery { api.flightOffers(any()) } returns Response.success("{}".toResponseBody())
        val q = FlightOffersQuery("LIM", "CUZ", "2027-08-01", adults = 2)
        assertTrue(repo.flightOffers(q.toQueryMap()).isSuccessful)
        coVerify { api.flightOffers(q.toQueryMap()) }
    }
}
