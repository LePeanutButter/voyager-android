package com.voyager.tourism.data.destination

/**
 * Coordenadas y código IATA aproximados según texto de destino (equivalente a
 * [voyager-web-client/src/utils/destinationGeoHints.js]).
 */
data class DestinationGeoHint(
    val lat: Double,
    val lng: Double,
    val cityCode: String,
    val label: String,
    val matched: Boolean,
)

private data class HintRule(val regex: Regex, val lat: Double, val lng: Double, val cityCode: String, val label: String)

private val HINT_RULES = listOf(
    HintRule(Regex("par[ií]s", RegexOption.IGNORE_CASE), 48.8566, 2.3522, "PAR", "París"),
    HintRule(Regex("barcelona", RegexOption.IGNORE_CASE), 41.3851, 2.1734, "BCN", "Barcelona"),
    HintRule(Regex("madrid", RegexOption.IGNORE_CASE), 40.4168, -3.7038, "MAD", "Madrid"),
    HintRule(Regex("valencia", RegexOption.IGNORE_CASE), 39.4699, -0.3763, "VLC", "Valencia"),
    HintRule(Regex("sevilla", RegexOption.IGNORE_CASE), 37.3891, -5.9845, "SVQ", "Sevilla"),
    HintRule(Regex("london|londres", RegexOption.IGNORE_CASE), 51.5074, -0.1278, "LON", "Londres"),
    HintRule(Regex("roma|rome", RegexOption.IGNORE_CASE), 41.9028, 12.4964, "FCO", "Roma"),
    HintRule(Regex("tokyo|tokio", RegexOption.IGNORE_CASE), 35.6762, 139.6503, "NRT", "Tokio"),
    HintRule(Regex("lisboa|lisbon", RegexOption.IGNORE_CASE), 38.7223, -9.1393, "LIS", "Lisboa"),
    HintRule(
        Regex("ciudad\\s*de\\s*m[eé]xico|mexico\\s*city|cdmx|ciudad\\s*de\\s*mexico", RegexOption.IGNORE_CASE),
        19.4326,
        -99.1332,
        "MEX",
        "Ciudad de México",
    ),
    HintRule(Regex("berlin|berl[ií]n", RegexOption.IGNORE_CASE), 52.52, 13.405, "BER", "Berlín"),
    HintRule(Regex("amsterdam", RegexOption.IGNORE_CASE), 52.3676, 4.9041, "AMS", "Ámsterdam"),
    HintRule(Regex("praga|prague", RegexOption.IGNORE_CASE), 50.0755, 14.4378, "PRG", "Praga"),
    HintRule(Regex("atenas|athens", RegexOption.IGNORE_CASE), 37.9838, 23.7275, "ATH", "Atenas"),
    HintRule(Regex("dublin", RegexOption.IGNORE_CASE), 53.3498, -6.2603, "DUB", "Dublín"),
    HintRule(Regex("nueva\\s*york|new\\s*york|nyc", RegexOption.IGNORE_CASE), 40.7128, -74.006, "NYC", "Nueva York"),
    HintRule(Regex("zurich|z[uü]rich", RegexOption.IGNORE_CASE), 47.3769, 8.5417, "ZRH", "Zúrich"),
    HintRule(Regex("oaxaca", RegexOption.IGNORE_CASE), 17.0732, -96.7266, "OAX", "Oaxaca"),
)

private val DEFAULT_HINT = DestinationGeoHint(40.4168, -3.7038, "MAD", "Madrid (por defecto)", false)

/** Normaliza IDs tipo `dst_barcelona` a texto buscable (mismo criterio que el web). */
fun normalizeDestinationSlugForSearch(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    var s = raw.trim()
    if (s.startsWith("dst_", ignoreCase = true)) {
        s = s.drop(4).replace('_', ' ')
    }
    return s.trim()
}

fun resolveDestinationHint(destinationText: String?): DestinationGeoHint {
    val t = destinationText?.trim().orEmpty()
    if (t.isEmpty()) return DEFAULT_HINT
    for (h in HINT_RULES) {
        if (h.regex.containsMatchIn(t)) {
            return DestinationGeoHint(h.lat, h.lng, h.cityCode, h.label, true)
        }
    }
    return DEFAULT_HINT
}

/** Etiqueta visible tipo [DestinationExplorePage.jsx] `destinationLabel`. */
fun buildDestinationExploreLabel(locRaw: String, countryRaw: String): String {
    val a = (normalizeDestinationSlugForSearch(locRaw).ifBlank { locRaw }).trim()
    val b = countryRaw.trim()
    return when {
        a.isNotEmpty() && b.isNotEmpty() -> "$a, $b"
        a.isNotEmpty() -> a
        b.isNotEmpty() -> b
        else -> "Destino"
    }
}
