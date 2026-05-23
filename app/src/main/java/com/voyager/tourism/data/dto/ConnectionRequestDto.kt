package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for connection requests between travelers
 */
@JsonClass(generateAdapter = true)
data class ConnectionRequestDto(
    @Json(name = "id")
    val id: Long,
    
    @Json(name = "recipientId")
    val recipientId: Long,
    
    @Json(name = "requesterId")
    val requesterId: Long,
    
    @Json(name = "status")
    val status: String, // PENDING, ACCEPTED, REJECTED
    
    @Json(name = "message")
    val message: String? = null,
    
    @Json(name = "createdAt")
    val createdAt: String,
    
    @Json(name = "updatedAt")
    val updatedAt: String,
    
    @Json(name = "requesterName")
    val requesterName: String? = null,
    
    @Json(name = "requesterProfileImage")
    val requesterProfileImage: String? = null,
    
    @Json(name = "recipientName")
    val recipientName: String? = null,
    
    @Json(name = "recipientProfileImage")
    val recipientProfileImage: String? = null
)

/**
 * DTO for sending connection requests
 */
@JsonClass(generateAdapter = true)
data class SendConnectionRequestDto(
    @Json(name = "recipientId")
    val recipientId: Long,
    
    @Json(name = "message")
    val message: String? = null
)
