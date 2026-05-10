package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.VoyagerAiApi
import com.voyager.tourism.data.dto.LocalRecommendationCandidateBody
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
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
        val body = LocalRecommendationRequestBody(
            userId = "42",
            query = "test",
            limit = 3,
            candidates = listOf(
                LocalRecommendationCandidateBody("a", "A", "c", 0.0, ""),
            ),
        )
        coEvery { api.postLocalRecommendations(body) } returns Response.success("{}".toResponseBody())
        assertTrue(repo.postLocalRecommendations(body).isSuccessful)
        coVerify { api.postLocalRecommendations(body) }
    }
}
