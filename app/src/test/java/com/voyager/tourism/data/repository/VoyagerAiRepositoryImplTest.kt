package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.VoyagerAiApi
import com.voyager.tourism.data.dto.AiDestinationRecommendationRequestBody
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class VoyagerAiRepositoryImplTest {

    @Test
    fun `delegates to VoyagerAiApi`() = runTest {
        val api = mockk<VoyagerAiApi>()
        val repo = VoyagerAiRepositoryImpl(api)
        val body = AiDestinationRecommendationRequestBody(userId = "42")
        coEvery { api.postDestinationsPersonalized(body) } returns Response.success("{}".toResponseBody())
        assertTrue(repo.postDestinationsPersonalized(body).isSuccessful)
        coVerify { api.postDestinationsPersonalized(body) }
    }
}
