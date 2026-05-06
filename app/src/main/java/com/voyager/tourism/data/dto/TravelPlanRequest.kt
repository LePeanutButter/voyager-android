package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object for travel plan creation requests
 * Contains all required fields for creating a new travel plan
 */
@JsonClass(generateAdapter = true)
data class TravelPlanRequest(
    @Json(name = "title")
    val title: String,
    
    @Json(name = "destination_location")
    val destinationLocation: String,
    
    @Json(name = "origin_location")
    val originLocation: String? = null,
    
    @Json(name = "start_date")
    val startDate: String, // ISO date string format
    
    @Json(name = "end_date")
    val endDate: String, // ISO date string format
    
    @Json(name = "estimated_budget")
    val estimatedBudget: Double? = null,
    
    @Json(name = "number_of_travelers")
    val numberOfTravelers: Int = 1,
    
    @Json(name = "description")
    val description: String? = null
)
