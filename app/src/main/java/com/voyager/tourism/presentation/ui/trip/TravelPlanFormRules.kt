package com.voyager.tourism.presentation.ui.trip

/** Alineado con [voyager-web-client/src/pages/TravelPlanning/CreateTravelPlanPage.jsx]. */
const val DESCRIPTION_MAX_LEN = 2000
const val COP_MIN = 50_000L
const val COP_STEP = 50L

fun digitsOnly(s: String): String = s.replace(Regex("\\D"), "")

fun roundCop(amount: Long): Long {
    val rounded = ((amount + COP_STEP / 2) / COP_STEP) * COP_STEP
    return maxOf(COP_MIN, rounded)
}

fun validateCopBudgetString(raw: String): String? {
    if (raw.isBlank()) return "Indica un presupuesto en COP (solo números)."
    val n = raw.toLongOrNull() ?: return "Presupuesto inválido."
    if (n < COP_MIN) return "Mínimo ${formatCop(COP_MIN)}."
    if (n % COP_STEP != 0L) return "Debe ser múltiplo de $COP_STEP COP."
    return null
}

fun formatCop(amount: Long): String =
    java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CO")).format(amount)
