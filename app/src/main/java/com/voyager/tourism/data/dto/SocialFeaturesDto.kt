package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponseDto<T>(
    @Json(name = "status") val status: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    @Json(name = "status") val status: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class ConnectionDto(
    @Json(name = "id") val id: Long,
    @Json(name = "username") val username: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class TravelPlanActivityDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class ShareActivityRequestDto(
    @Json(name = "receiverId") val receiverId: Long
)

@JsonClass(generateAdapter = true)
data class SharedActivityActionRequestDto(
    @Json(name = "action") val action: String
)

@JsonClass(generateAdapter = true)
data class SharedActivityResponseDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "activityId") val activityId: Long? = null,
    @Json(name = "senderId") val senderId: Long? = null,
    @Json(name = "receiverId") val receiverId: Long? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "sharedPlan") val sharedPlan: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class CompatibilityMatchRequestDto(
    @Json(name = "destination") val destination: String,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "interests") val interests: List<String>
)

@JsonClass(generateAdapter = true)
data class CompatibilityMatchResponseDto(
    @Json(name = "userId") val userId: Long,
    @Json(name = "totalScore") val totalScore: Double,
    @Json(name = "destinationScore") val destinationScore: Double,
    @Json(name = "dateProximityScore") val dateProximityScore: Double,
    @Json(name = "interestScore") val interestScore: Double,
    @Json(name = "matchedInterests") val matchedInterests: List<String> = emptyList()
)
