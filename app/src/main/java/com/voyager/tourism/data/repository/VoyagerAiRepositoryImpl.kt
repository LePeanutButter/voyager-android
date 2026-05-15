package com.voyager.tourism.data.repository

import com.squareup.moshi.Moshi
import com.voyager.tourism.data.api.VoyagerAiApi
import com.voyager.tourism.data.dto.AiUserInsightsDto
import com.voyager.tourism.data.dto.SegmentInsightsDto
import com.voyager.tourism.data.dto.WeeklyDigestDto
import com.voyager.tourism.data.dto.LocalChatResponseDto
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

/**
 * Repository that wraps Retrofit `VoyagerAiApi` and converts loosely-typed Map responses
 * into strongly-typed DTOs using Moshi adapters for the mobile client.
 */
@Singleton
class VoyagerAiRepositoryImpl @Inject constructor(
    private val api: VoyagerAiApi,
    private val moshi: Moshi,
) : VoyagerAiRepository, VoyagerAiApi by api {

    override suspend fun getUserInsightsTyped(userId: String): Response<AiUserInsightsDto> {
        val resp = api.getUserInsights(userId)
        if (!resp.isSuccessful) return Response.error(resp.code(), resp.errorBody() ?: "".toResponseBody(null))
        val raw = resp.body()
        val adapter = moshi.adapter(AiUserInsightsDto::class.java)
        return try {
            val dto = if (raw != null) adapter.fromJsonValue(raw) else AiUserInsightsDto(raw = null)
            Response.success(dto)
        } catch (e: Exception) {
            val fallback = AiUserInsightsDto(raw = raw as? Map<String, Any>)
            Response.success(fallback)
        }
    }

    override suspend fun getSegmentInsightsTyped(segmentId: String): Response<SegmentInsightsDto> {
        val resp = api.getSegmentInsights(segmentId)
        if (!resp.isSuccessful) return Response.error(resp.code(), resp.errorBody() ?: "".toResponseBody(null))
        val raw = resp.body()
        val adapter = moshi.adapter(SegmentInsightsDto::class.java)
        return try {
            val dto = if (raw != null) adapter.fromJsonValue(raw) else SegmentInsightsDto(raw = null)
            Response.success(dto)
        } catch (e: Exception) {
            val fallback = SegmentInsightsDto(raw = raw as? Map<String, Any>)
            Response.success(fallback)
        }
    }

    override suspend fun getWeeklyDigestTyped(): Response<WeeklyDigestDto> {
        val resp = api.getWeeklyDigest()
        if (!resp.isSuccessful) return Response.error(resp.code(), resp.errorBody() ?: "".toResponseBody(null))
        val raw = resp.body()
        val adapter = moshi.adapter(WeeklyDigestDto::class.java)
        return try {
            val dto = if (raw != null) adapter.fromJsonValue(raw) else WeeklyDigestDto(raw = null)
            Response.success(dto)
        } catch (e: Exception) {
            val fallback = WeeklyDigestDto(raw = raw as? Map<String, Any>)
            Response.success(fallback)
        }
    }

    override suspend fun getLocalChatHistoryTyped(sessionId: String, limit: Int): Response<List<LocalChatResponseDto>> {
        // API already returns typed LocalChatResponseDto list; delegate directly
        return api.getLocalChatHistory(sessionId, limit)
    }

    // For other API surfaces not specialized above, delegate to the underlying API.
    // Consumers can cast the repository to VoyagerAiApi if they need full access, or we
    // can add explicit wrappers as needed.
    @Suppress("UNCHECKED_CAST")
    private fun <T> delegated(): T = api as T
}
