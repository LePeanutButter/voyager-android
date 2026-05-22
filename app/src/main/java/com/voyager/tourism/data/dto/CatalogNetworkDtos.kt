package com.voyager.tourism.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Simplified flight offer data.
 */
@JsonClass(generateAdapter = true)
data class FlightOfferDto(
    @Json(name = "id") val id: String,
    @Json(name = "itineraries") val itineraries: List<FlightItineraryDto>,
    @Json(name = "price") val price: FlightPriceDto,
    @Json(name = "validatingAirlineCodes") val validatingAirlineCodes: List<String>
)

@JsonClass(generateAdapter = true)
data class FlightItineraryDto(
    @Json(name = "duration") val duration: String,
    @Json(name = "segments") val segments: List<FlightSegmentDto>
)

@JsonClass(generateAdapter = true)
data class FlightSegmentDto(
    @Json(name = "departure") val departure: FlightEndpointDto,
    @Json(name = "arrival") val arrival: FlightEndpointDto,
    @Json(name = "carrierCode") val carrierCode: String,
    @Json(name = "number") val number: String,
    @Json(name = "duration") val duration: String
)

@JsonClass(generateAdapter = true)
data class FlightEndpointDto(
    @Json(name = "iataCode") val iataCode: String,
    @Json(name = "at") val at: String
)

@JsonClass(generateAdapter = true)
data class FlightPriceDto(
    @Json(name = "currency") val currency: String,
    @Json(name = "total") val total: String,
    @Json(name = "base") val base: String
)

/**
 * Simplified hotel data.
 */
@JsonClass(generateAdapter = true)
data class HotelDto(
    @Json(name = "hotelId") val hotelId: String,
    @Json(name = "name") val name: String,
    @Json(name = "iataCode") val iataCode: String,
    @Json(name = "address") val address: Map<String, String>? = null,
    @Json(name = "geoCode") val geoCode: Map<String, Double>? = null
)

/**
 * Simplified activity/POI data.
 */
@JsonClass(generateAdapter = true)
data class ActivityDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "short_description") val shortDescription: String? = null,
    @Json(name = "rating") val rating: String? = null,
    @Json(name = "price") val price: Map<String, Any>? = null,
    @Json(name = "pictures") val pictures: List<String> = emptyList(),
    @Json(name = "booking_link") val bookingLink: String? = null
)
