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

    fun parseWeeklyDigestLines(json: String): List<String> {
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
                    val line = lineFromDigestItem(arr.opt(i))
                    if (line != null) add(line)
                }
            }.take(3)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseSeasonalityLines(json: String): List<String> {
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
                    val line = lineFromSeasonalityItem(arr.opt(i))
                    if (line != null) add(line)
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

    private fun lineFromDigestItem(v: Any?): String? {
        if (v is JSONObject) {
            val t = v.optString("title").ifBlank { v.optString("headline") }
                .ifBlank { v.optString("name") }
                .ifBlank { v.optString("summary") }
            if (t.isNotBlank()) return t
            val geo = v.optJSONObject("geo")
            if (geo != null) {
                val n = geo.optString("name").ifBlank { geo.optString("destination_id") }
                if (n.isNotBlank()) return n
            }
        }
        return null
    }

    private fun lineFromSeasonalityItem(v: Any?): String? {
        if (v is JSONObject) {
            val name = v.optString("name").ifBlank { v.optString("destination_name") }
                .ifBlank { v.optString("destinationName") }
                .ifBlank { v.optString("label") }
            if (name.isNotBlank()) return name
        }
        return null
    }
}
