package com.voyager.tourism.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TravelPlanActivityDto(
    val id: String,
    val name: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val location: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class UpdateActivityRequest(
    val name: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val location: String?
)

@JsonClass(generateAdapter = true)
data class CreateActivityRequest(
    val name: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val location: String?
)
