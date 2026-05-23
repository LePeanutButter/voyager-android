package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Generic typed DTOs for loosely-structured AI responses. These include a `raw` map fallback
 * for fields that may vary across deployments; callers should prefer the typed fields.
 */
@JsonClass(generateAdapter = true)
data class AiUserInsightsDto(
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "metrics") val metrics: Map<String, Double>? = null,
    // Fallback for any additional keys returned by the service
    @Json(name = "raw") val raw: Map<String, Any>? = null,
)

@JsonClass(generateAdapter = true)
data class WeeklyDigestDto(
    @Json(name = "title") val title: String? = null,
    @Json(name = "entries") val entries: List<Map<String, Any>>? = null,
    @Json(name = "raw") val raw: Map<String, Any>? = null,
)

@JsonClass(generateAdapter = true)
data class SegmentInsightsDto(
    @Json(name = "segment_id") val segmentId: String? = null,
    @Json(name = "insights") val insights: Map<String, Any>? = null,
    @Json(name = "raw") val raw: Map<String, Any>? = null,
)
