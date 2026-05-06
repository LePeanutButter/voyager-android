package com.voyager.tourism.data.dto

import com.squareup.moshi.JsonClass

/**
 * Short public profile snippet for another traveler shown in social or discovery UI.
 */
@JsonClass(generateAdapter = true)
data class TravelerSummaryDto(
    val userId: String,
    val displayName: String,
    val bioShort: String,
    val profileImageUrl: String?,
    val interests: String
)
