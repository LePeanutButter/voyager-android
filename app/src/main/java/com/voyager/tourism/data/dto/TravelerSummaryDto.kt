package com.voyager.tourism.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TravelerSummaryDto(
    val userId: String,
    val displayName: String,
    val bioShort: String,
    val profileImageUrl: String?,
    val interests: String
)
