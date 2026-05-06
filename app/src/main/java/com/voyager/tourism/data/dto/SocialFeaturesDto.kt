package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Generic wrapper for API payloads that include HTTP-style status and message fields.
 */
@JsonClass(generateAdapter = true)
data class ApiResponseDto<T>(
    @Json(name = "status") val status: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null
)

/**
 * Normalized error payload returned alongside non-success HTTP semantics.
 */
@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    @Json(name = "status") val status: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null
)

/**
 * Accepted traveler connection as returned by the backend (`TravelConnectionDto`).
 */
@JsonClass(generateAdapter = true)
data class ConnectionDto(
    @Json(name = "userId") val userId: Long,
    @Json(name = "username") val username: String,
    @Json(name = "firstName") val firstName: String? = null,
    @Json(name = "lastName") val lastName: String? = null,
    @Json(name = "status") val status: String,
)

/**
 * Asks the backend to share an activity with another user by recipient id.
 */
@JsonClass(generateAdapter = true)
data class ShareActivityRequestDto(
    @Json(name = "receiverId") val receiverId: Long
)

/**
 * User action when accepting or rejecting a shared activity proposal.
 */
@JsonClass(generateAdapter = true)
data class SharedActivityActionRequestDto(
    @Json(name = "action") val action: String
)

/**
 * Server state for a shared activity after create or update operations.
 */
@JsonClass(generateAdapter = true)
data class SharedActivityResponseDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "activityId") val activityId: Long? = null,
    @Json(name = "senderId") val senderId: Long? = null,
    @Json(name = "receiverId") val receiverId: Long? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "sharedPlan") val sharedPlan: Boolean? = null
)

/**
 * Criteria sent when requesting traveler compatibility scoring for a trip window.
 */
@JsonClass(generateAdapter = true)
data class CompatibilityMatchRequestDto(
    @Json(name = "destination") val destination: String,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "interests") val interests: List<String>
)

/**
 * Breakdown of compatibility scoring returned for a potential match.
 */
@JsonClass(generateAdapter = true)
data class CompatibilityMatchResponseDto(
    @Json(name = "userId") val userId: Long,
    @Json(name = "totalScore") val totalScore: Double,
    @Json(name = "destinationScore") val destinationScore: Double,
    @Json(name = "dateProximityScore") val dateProximityScore: Double,
    @Json(name = "interestScore") val interestScore: Double,
    @Json(name = "matchedInterests") val matchedInterests: List<String> = emptyList()
)
