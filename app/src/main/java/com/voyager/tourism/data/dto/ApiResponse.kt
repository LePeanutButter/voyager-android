package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Generic API response wrapper for all backend endpoints
 * Matches exactly the backend ApiResponse structure
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "timestamp")
    val timestamp: String, // LocalDateTime from backend, parsed as String
    
    @Json(name = "status")
    val status: Int,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "data")
    val data: T?,
    
    @Json(name = "path")
    val path: String? = null,
    
    @Json(name = "errors")
    val errors: List<ValidationError>? = null
)

@JsonClass(generateAdapter = true)
data class ValidationError(
    @Json(name = "field")
    val field: String,
    
    @Json(name = "message")
    val message: String
)
