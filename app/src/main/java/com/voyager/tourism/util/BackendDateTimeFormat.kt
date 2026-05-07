package com.voyager.tourism.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Formatea instantes ISO-8601 tipo `LocalDateTime` para UI; si el parse falla devuelve el string original.
 */
fun formatBackendLocalDateTime(dateString: String, pattern: String): String {
    return try {
        LocalDateTime.parse(dateString).format(DateTimeFormatter.ofPattern(pattern))
    } catch (_: Exception) {
        dateString
    }
}
