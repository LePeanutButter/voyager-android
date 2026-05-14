package com.voyager.tourism.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import okhttp3.ResponseBody

/**
 * Parámetros de búsqueda de vuelos para [CatalogApiService.flightOffers] (mapa de query sin romper Retrofit).
 */
data class FlightOffersQuery(
    val originLocationCode: String,
    val destinationLocationCode: String,
    val departureDate: String,
    val adults: Int = 1,
    val returnDate: String? = null,
    val children: Int? = null,
    val max: Int? = null,
    val travelClass: String? = null,
    val nonStop: Boolean? = null,
    val currencyCode: String? = null,
) {
    fun toQueryMap(): Map<String, String> = buildMap {
        put("originLocationCode", originLocationCode)
        put("destinationLocationCode", destinationLocationCode)
        put("departureDate", departureDate)
        put("adults", adults.toString())
        returnDate?.takeIf { it.isNotBlank() }?.let { put("returnDate", it) }
        children?.let { put("children", it.toString()) }
        max?.let { put("max", it.toString()) }
        travelClass?.takeIf { it.isNotBlank() }?.let { put("travelClass", it) }
        nonStop?.let { put("nonStop", it.toString()) }
        currencyCode?.takeIf { it.isNotBlank() }?.let { put("currencyCode", it) }
    }
}

/**
 * Amadeus-backed catalog proxy ([com.tourism.platform.controller.TravelCatalogController]).
 * JSON payloads (including `ApiResponse` wrappers) are returned as raw [ResponseBody] for faithful backend parsing.
 */
interface CatalogApiService {

    /**
     * Searches flight offers between an origin and destination IATA code on a departure date.
     */
    @GET("catalog/flights")
    suspend fun flightOffers(@QueryMap queries: Map<String, String>): ApiResponse<List<FlightOfferDto>>

    /**
     * Lists candidate hotels for a given IATA city code.
     */
    @GET("catalog/hotels/by-city")
    suspend fun hotelsByCity(@Query("cityCode") cityCode: String): ApiResponse<List<HotelDto>>

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
    ): ApiResponse<List<Map<String, Any>>> // Hotel offers can be very complex, using a map for the inner offer part but wrapped in ApiResponse

    /**
     * Queries point-of-interest activities around a latitude/longitude with radius filters.
     */
    @GET("catalog/activities")
    suspend fun activities(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double = 5.0,
        @Query("radiusUnit") radiusUnit: String = "KM",
    ): ApiResponse<List<ActivityDto>>
}

