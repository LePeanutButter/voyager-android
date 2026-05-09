package com.voyager.tourism.presentation.ui.trip

import com.voyager.tourism.domain.model.TripStatus

/**
 * Etiquetas como en [voyager-web-client/src/pages/MyTravels/MyTravels.jsx] (`statusConfig`).
 */
fun TripStatus.spanishLabel(): String = when (this) {
    TripStatus.PLANNING -> "Planificando"
    TripStatus.CONFIRMED -> "Confirmado"
    TripStatus.ACTIVE -> "Activo"
    TripStatus.COMPLETED -> "Completado"
    TripStatus.CANCELLED -> "Cancelado"
}
