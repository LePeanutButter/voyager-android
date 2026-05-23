package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.api.VoyagerAiApi
import com.voyager.tourism.data.dto.AiUserInsightsDto
import com.voyager.tourism.data.dto.SegmentInsightsDto
import com.voyager.tourism.data.dto.WeeklyDigestDto
import com.voyager.tourism.data.dto.LocalChatResponseDto
import retrofit2.Response

/**
 * Repository that combines the full `VoyagerAiApi` surface with additional
 * typed helpers for heterogeneous AI responses. This preserves backward
 * compatibility for existing callers while providing typed DTO accessors.
 */
interface VoyagerAiRepository : VoyagerAiApi {
	suspend fun getUserInsightsTyped(userId: String): Response<AiUserInsightsDto>

	suspend fun getSegmentInsightsTyped(segmentId: String): Response<SegmentInsightsDto>

	suspend fun getWeeklyDigestTyped(): Response<WeeklyDigestDto>

	suspend fun getLocalChatHistoryTyped(sessionId: String, limit: Int = 50): Response<List<LocalChatResponseDto>>
}
