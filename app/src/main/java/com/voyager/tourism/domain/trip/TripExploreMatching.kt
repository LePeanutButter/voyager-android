package com.voyager.tourism.domain.trip

import com.voyager.tourism.domain.model.Trip

/** Primera parte de la etiqueta antes de coma (como `primaryPlace` en [DestinationExplorePage.jsx]). */
fun primaryPlaceToken(destinationLabel: String): String =
    destinationLabel.split(',').firstOrNull()?.trim()?.lowercase().orEmpty()

/**
 * Indica si un [Trip] corresponde a la etiqueta de destino mostrada en explorar (nombre, título, país, id).
 */
fun tripMatchesDestinationLabel(trip: Trip, destinationLabel: String): Boolean {
    val primary = primaryPlaceToken(destinationLabel)
    if (primary.isBlank()) return false
    val loc = trip.destination.name.lowercase()
    val title = trip.title.lowercase()
    val country = trip.destination.country.lowercase()
    val idNorm = trip.destination.id
        .removePrefix("dst_")
        .replace('_', ' ')
        .lowercase()
    return loc.contains(primary) ||
        title.contains(primary) ||
        country.contains(primary) ||
        trip.destination.id.equals(primary, ignoreCase = true) ||
        idNorm.contains(primary)
}
