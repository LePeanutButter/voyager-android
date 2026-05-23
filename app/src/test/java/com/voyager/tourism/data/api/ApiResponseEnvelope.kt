package com.voyager.tourism.data.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.voyager.tourism.data.dto.ApiResponse

/**
 * Builds JSON bodies that match the backend [ApiResponse] envelope so MockWebServer responses
 * deserialize exactly like production traffic.
 */
object ApiResponseEnvelope {

    fun <T> success(
        moshi: Moshi,
        data: T?,
        dataClass: Class<T>,
        status: Int = 200,
        message: String = "OK",
        path: String? = null,
    ): String {
        val envelope = ApiResponse(
            timestamp = "2026-05-06T12:00:00",
            status = status,
            message = message,
            data = data,
            path = path,
            errors = null,
        )
        return adapter<T>(moshi, dataClass).toJson(envelope)
    }

    fun <T> adapter(moshi: Moshi, dataClass: Class<T>): JsonAdapter<ApiResponse<T>> {
        val type = Types.newParameterizedType(ApiResponse::class.java, dataClass)
        return moshi.adapter(type)
    }

    /**
     * Envelope for `ApiResponse<List<E>>` (e.g. compatible travelers, pending requests).
     */
    fun <E> successList(
        moshi: Moshi,
        data: List<E>,
        elementClass: Class<E>,
        status: Int = 200,
        message: String = "OK",
    ): String {
        val listType = Types.newParameterizedType(List::class.java, elementClass)
        val apiType = Types.newParameterizedType(ApiResponse::class.java, listType)
        val adapter = moshi.adapter<ApiResponse<List<E>>>(apiType)
        val envelope = ApiResponse(
            timestamp = "2026-05-06T12:00:00",
            status = status,
            message = message,
            data = data,
            path = null,
            errors = null,
        )
        return adapter.toJson(envelope)
    }
}
