package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PagedResponseUserDto(
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "status") val status: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: List<UserDto>? = null,
    @Json(name = "currentPage") val currentPage: Int = 0,
    @Json(name = "totalPages") val totalPages: Int = 0,
    @Json(name = "totalElements") val totalElements: Long = 0,
    @Json(name = "pageSize") val pageSize: Int = 0,
    @Json(name = "first") val first: Boolean = false,
    @Json(name = "last") val last: Boolean = false,
    @Json(name = "path") val path: String? = null,
)

@JsonClass(generateAdapter = true)
data class PagedResponseTravelPlanDto(
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "status") val status: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: List<TravelPlanDto>? = null,
    @Json(name = "currentPage") val currentPage: Int = 0,
    @Json(name = "totalPages") val totalPages: Int = 0,
    @Json(name = "totalElements") val totalElements: Long = 0,
    @Json(name = "pageSize") val pageSize: Int = 0,
    @Json(name = "first") val first: Boolean = false,
    @Json(name = "last") val last: Boolean = false,
    @Json(name = "path") val path: String? = null,
)

@JsonClass(generateAdapter = true)
data class PagedResponseMessageDto(
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "status") val status: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: List<MessageDto>? = null,
    @Json(name = "currentPage") val currentPage: Int = 0,
    @Json(name = "totalPages") val totalPages: Int = 0,
    @Json(name = "totalElements") val totalElements: Long = 0,
    @Json(name = "pageSize") val pageSize: Int = 0,
    @Json(name = "first") val first: Boolean = false,
    @Json(name = "last") val last: Boolean = false,
    @Json(name = "path") val path: String? = null,
)

@JsonClass(generateAdapter = true)
data class UserStatisticsDto(
    @Json(name = "totalUsers") val totalUsers: Long = 0,
    @Json(name = "activeUsers") val activeUsers: Long = 0,
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "connectionId") val connectionId: Long? = null,
    @Json(name = "senderId") val senderId: Long? = null,
    @Json(name = "recipientId") val recipientId: Long? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SendMessageRequestDto(
    @Json(name = "connectionId") val connectionId: Long,
    @Json(name = "senderId") val senderId: Long,
    @Json(name = "content") val content: String,
)

@JsonClass(generateAdapter = true)
data class MatchResponseDto(
    @Json(name = "userId") val userId: Long,
    @Json(name = "username") val username: String? = null,
    @Json(name = "destination") val destination: String? = null,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "destinationPoints") val destinationPoints: Int? = null,
    @Json(name = "datePoints") val datePoints: Int? = null,
    @Json(name = "interestPoints") val interestPoints: Int? = null,
)

@JsonClass(generateAdapter = true)
data class ReservationDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "confirmationNumber") val confirmationNumber: String? = null,
    @Json(name = "startDate") val startDate: String? = null,
    @Json(name = "endDate") val endDate: String? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "totalCost") val totalCost: Double? = null,
    @Json(name = "isPaid") val isPaid: Boolean? = null,
)
