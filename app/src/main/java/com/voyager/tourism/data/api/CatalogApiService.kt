package com.voyager.tourism.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.ResponseBody

/**
 * Amadeus-backed catalog proxy ([com.tourism.platform.controller.TravelCatalogController]).
 * JSON payloads (including `ApiResponse` wrappers) are returned as raw [ResponseBody] for faithful backend parsing.
 */
interface CatalogApiService {

    /**
     * Searches flight offers between an origin and destination IATA code on a departure date.
     */
    @GET("catalog/flights")
    suspend fun flightOffers(
        @Query("originLocationCode") originLocationCode: String,
        @Query("destinationLocationCode") destinationLocationCode: String,
        @Query("departureDate") departureDate: String,
        @Query("adults") adults: Int = 1,
        @Query("returnDate") returnDate: String? = null,
        @Query("children") children: Int? = null,
        @Query("max") max: Int? = null,
        @Query("travelClass") travelClass: String? = null,
        @Query("nonStop") nonStop: Boolean? = null,
        @Query("currencyCode") currencyCode: String? = null,
    ): Response<ResponseBody>

    /**
     * Lists candidate hotels for a given IATA city code.
     */
    @GET("catalog/hotels/by-city")
    suspend fun hotelsByCity(@Query("cityCode") cityCode: String): Response<ResponseBody>

    /**
     * Retrieves concrete hotel offer pricing for selected hotel ids and stay window.
     */
    @GET("catalog/hotels/offers")
    suspend fun hotelOffers(
        @Query("hotelIds") hotelIds: String,
        @Query("checkInDate") checkInDate: String,
        @Query("checkOutDate") checkOutDate: String,
        @Query("adults") adults: Int = 1,
        @Query("rooms") rooms: Int = 1,
        @Query("currency") currency: String? = null,
    ): Response<ResponseBody>

    /**
     * Queries point-of-interest activities around a latitude/longitude with radius filters.
     */
    @GET("catalog/activities")
    suspend fun activities(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double = 5.0,
        @Query("radiusUnit") radiusUnit: String = "KM",
    ): Response<ResponseBody>
}
