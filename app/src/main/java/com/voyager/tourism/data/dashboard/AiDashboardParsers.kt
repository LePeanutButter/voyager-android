package com.voyager.tourism.data.dashboard

import org.json.JSONArray
import org.json.JSONObject

/**
 * Parseo defensivo de respuestas del voyager-ai-service alineadas con
 * [voyager-web-client/src/pages/Dashboard/Dashboard.jsx] (el web además pasa por camelCase en axios).
 */
data class ParsedTrendingDestination(
    val id: String?,
    val name: String,
    val country: String,
)

/** Fila del digest semanal (micro-tendencias), alineada con [Dashboard.jsx] trendsDigest. */
data class ParsedDigestRow(
    val title: String,
    val subtitle: String,
    /** Texto para búsqueda / recomendaciones cuando no hay destId de catálogo. */
    val exploreQuery: String,
    val country: String?,
    val destId: String?,
)

/** Fila de panorama estacional, alineada con [Dashboard.jsx] seasonalityHighlights. */
data class ParsedSeasonalityRow(
    val id: String?,
    val title: String,
    val subtitle: String,
)

object AiDashboardParsers {

    fun unwrapDataObject(root: JSONObject): JSONObject {
        val data = root.opt("data")
        return if (data is JSONObject) data else root
    }

    fun parseTrendsDashboard(json: String): List<ParsedTrendingDestination> {
        val raw = json.trim()
        if (raw.isEmpty()) return emptyList()
        return try {
            val payload = unwrapDataObject(JSONObject(raw))
            val arr = payload.optJSONArray("emerging_destinations")
                ?: payload.optJSONArray("emergingDestinations")
                ?: return emptyList()
            buildList {
                for (i in 0 until minOf(arr.length(), 8)) {
                    val o = arr.optJSONObject(i) ?: continue
                    val name = o.optString("name").ifBlank { o.optString("title") }
                    if (name.isBlank()) continue
                    val id = o.optString("destination_id").ifBlank { o.optString("destinationId") }.ifBlank { null }
                    val country = o.optString("country").ifBlank { "" }
                    add(ParsedTrendingDestination(id = id, name = name, country = country))
                }
            }.take(3)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseWeeklyDigestLines(json: String): List<String> =
        parseWeeklyDigestRows(json).map { row ->
            if (row.subtitle.isNotBlank()) "${row.title} — ${row.subtitle}" else row.title
        }

    fun parseWeeklyDigestRows(json: String): List<ParsedDigestRow> {
        val raw = json.trim()
        if (raw.isEmpty()) return emptyList()
        return try {
            val payload = unwrapDataObject(JSONObject(raw))
            val arr = firstArray(
                payload,
                "micro_trends",
                "microTrends",
                "trends",
                "items",
                "highlights",
            ) ?: return emptyList()
            buildList {
                for (i in 0 until minOf(arr.length(), 8)) {
                    val row = digestRowFromItem(arr.opt(i))
                    if (row != null) add(row)
                }
            }.take(3)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseSeasonalityLines(json: String): List<String> =
        parseSeasonalityRows(json).map { it.title }

    fun parseSeasonalityRows(json: String): List<ParsedSeasonalityRow> {
        val raw = json.trim()
        if (raw.isEmpty()) return emptyList()
        return try {
            val payload = unwrapDataObject(JSONObject(raw))
            val arr = firstArray(
                payload,
                "destinations",
                "profiles",
                "seasonality_profiles",
                "seasonalityProfiles",
                "items",
            ) ?: return emptyList()
            buildList {
                for (i in 0 until minOf(arr.length(), 8)) {
                    val row = seasonalityRowFromItem(arr.opt(i))
                    if (row != null) add(row)
                }
            }.take(3)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun firstArray(obj: JSONObject, vararg keys: String): JSONArray? {
        for (k in keys) {
            val v = obj.opt(k)
            if (v is JSONArray && v.length() > 0) return v
        }
        return null
    }

    private fun digestRowFromItem(v: Any?): ParsedDigestRow? {
        if (v !is JSONObject) return null
        val g = v.optJSONObject("geo")
        val locFromGeo = g?.optString("name")?.trim().orEmpty()
        val slugFromDigestId = (g?.optString("destination_id") ?: "").ifBlank { g?.optString("destinationId") ?: "" }.trim()
        val geo = listOf(
            locFromGeo,
            slugFromDigestId,
            v.optString("destination").trim(),
            v.optString("city").trim(),
            v.optString("region").trim(),
            v.optString("primaryDestination").trim(),
            v.optString("primary_destination").trim(),
            v.optString("affectedDestination").trim(),
            v.optString("affected_destination").trim(),
        ).firstOrNull { it.isNotBlank() }
        val country = g?.optString("country")?.trim()?.takeIf { it.isNotBlank() }
            ?: v.optString("country").trim().takeIf { it.isNotBlank() }
        val destId = (g?.optString("destination_id") ?: "").ifBlank { g?.optString("destinationId") ?: "" }.trim()
            .ifBlank { v.optString("trendId").trim() }
            .ifBlank { v.optString("trend_id").trim() }
            .ifBlank { v.optString("id").trim() }
            .takeIf { it.isNotBlank() }
        val title = v.optString("title").ifBlank { v.optString("headline") }
            .ifBlank { v.optString("name") }
            .ifBlank { v.optString("destination") }
            .ifBlank { geo.orEmpty() }
            .ifBlank { return null }
        val subtitle = v.optString("summary").ifBlank { v.optString("description") }
            .ifBlank { v.optString("signal").trim() }
            .ifBlank { v.optString("type").trim() }
        val exploreQuery = geo?.takeIf { it.isNotBlank() } ?: title
        return ParsedDigestRow(
            title = title,
            subtitle = subtitle,
            exploreQuery = exploreQuery,
            country = country,
            destId = destId,
        )
    }

    private fun seasonalityRowFromItem(v: Any?): ParsedSeasonalityRow? {
        if (v !is JSONObject) return null
        val rawDest = v.optString("destination").ifBlank { v.optString("destinationId") }
            .ifBlank { v.optString("destination_id") }
            .ifBlank { v.optString("name") }
            .ifBlank { v.optString("destination_name") }
            .ifBlank { v.optString("destinationName") }
            .ifBlank { v.optString("label") }
            .trim()
        if (rawDest.isBlank()) return null
        val title = v.optString("destination").ifBlank { rawDest }
        val subtitle = v.optString("note").ifBlank { v.optString("summary") }
            .ifBlank { v.optString("label") }
            .ifBlank { "Perfil estacional disponible" }
        val id = v.optString("destinationId").ifBlank { v.optString("destination_id") }
            .ifBlank { v.optString("id") }
            .takeIf { it.isNotBlank() }
        return ParsedSeasonalityRow(id = id, title = title.trim(), subtitle = subtitle.trim())
    }
}
