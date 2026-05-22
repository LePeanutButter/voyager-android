package com.voyager.tourism.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.voyager.tourism.data.api.VoyagerAiApi
import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.LocalRecommendationCandidateBody
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class VoyagerAiRepositoryImplTest {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test
    fun `delegates to VoyagerAiApi`() = runTest {
        val api = mockk<VoyagerAiApi>()
        val repo = VoyagerAiRepositoryImpl(api, moshi)
        val body = LocalRecommendationRequestBody(
            userId = "42",
            query = "test",
            limit = 3,
            candidates = listOf(
                LocalRecommendationCandidateBody("a", "A", "c", 0.0, ""),
            ),
        )
        val mockResponse = com.voyager.tourism.data.dto.LocalRecommendationResponseDto(
            items = emptyList()
        )
        coEvery { api.postLocalRecommendations(body) } returns Response.success(mockResponse)
        assertTrue(repo.postLocalRecommendations(body).isSuccessful)
        coVerify { api.postLocalRecommendations(body) }
    }
}
