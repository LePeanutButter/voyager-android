package com.voyager.tourism.data.catalog

import org.json.JSONArray
import org.json.JSONObject

/**
 * Fila de actividad del catálogo Amadeus (equivalente a [normalizeCatalogActivity] en el web).
 */
data class CatalogActivityRow(
    val id: String,
    val name: String,
    val description: String,
)

object CatalogActivitiesParser {

    private fun extractDataArray(root: JSONObject): JSONArray {
        return when (val data = root.opt("data")) {
            is JSONArray -> data
            is JSONObject ->
                data.optJSONArray("activities")
                    ?: data.optJSONArray("items")
                    ?: data.optJSONArray("data")
                    ?: JSONArray()
            else -> JSONArray()
        }
    }

    fun parseActivitiesJson(json: String): List<CatalogActivityRow> {
        val raw = json.trim()
        if (raw.isEmpty()) return emptyList()
        return try {
            val root = JSONObject(raw)
            val arr = extractDataArray(root)
            if (arr.length() == 0) return emptyList()
            buildList {
                for (i in 0 until minOf(arr.length(), 40)) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.optString("id").ifBlank { "act-${o.optString("name")}-$i" }
                    val name = o.optString("name").ifBlank { o.optString("title") }.trim()
                    if (name.isBlank()) continue
                    val description = o.optString("shortDescription").ifBlank { o.optString("description") }
                    add(CatalogActivityRow(id = id, name = name, description = description))
                }
            }.take(14)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
