package com.voyager.tourism.data.util

import com.voyager.tourism.data.dto.ApiResponse

/**
 * Utilities to consistently extract payloads from `ApiResponse<T>` when backends
 * may return either a direct `data: List<T>` or a page-like object with nested `content`.
 */
object ApiUnwrapper {
    /**
     * Returns the list payload whether `data` is directly the list or a wrapper that contains `content`.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> extractList(response: ApiResponse<Any?>): List<T> {
        val data = response.data ?: return emptyList()
        // Direct list
        if (data is List<*>) return data as List<T>
        // Page-like with content field
        try {
            val map = data as? Map<*, *>
            val content = map?.get("content")
            if (content is List<*>) return content as List<T>
            val inner = map?.get("data")
            if (inner is List<*>) return inner as List<T>
        } catch (e: Exception) {
            return emptyList()
        }
        return emptyList()
    }
}
