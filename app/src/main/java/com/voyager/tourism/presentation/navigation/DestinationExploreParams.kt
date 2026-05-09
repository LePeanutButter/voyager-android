package com.voyager.tourism.presentation.navigation

/**
 * Parámetros equivalentes a `/explore/destination?loc=&country=&destId=` del web.
 */
data class DestinationExploreParams(
    val loc: String,
    val country: String? = null,
    val destId: String? = null,
)
