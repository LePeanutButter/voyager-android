package com.voyager.tourism.data.localai

import org.json.JSONObject

/**
 * Parseo defensivo de [POST /local/recommendations](voyager-ai-service) alineado con
 * `RecommendationResponse.items` (dicts con id, name, category, score/similarity, etc.).
 */
data class ParsedLocalRecommendationItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val rating: Float,
    val priceLabel: String,
)

object LocalRecommendationParsers {

    fun parseItems(json: String): List<ParsedLocalRecommendationItem> {
        val raw = json.trim()
        if (raw.isEmpty()) return emptyList()
        return try {
            val root = JSONObject(raw)
            val arr = root.optJSONArray("items") ?: return emptyList()
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val id = o.optString("id")
                    if (id.isBlank()) continue
                    val name = o.optString("name").ifBlank { id }
                    val category = o.optString("category").ifBlank { "general" }
                    val desc = o.optString("content_text")
                        .ifBlank { o.optString("description").ifBlank { name } }
                    val score = when {
                        o.has("score") -> o.optDouble("score", 0.0).toFloat().coerceIn(0f, 1f)
                        o.has("similarity") -> o.optDouble("similarity", 0.0).toFloat().coerceIn(0f, 1f)
                        else -> 0f
                    }
                    val rating = (score * 5f).coerceIn(0f, 5f)
                    val priceLabel = priceTierFromScore(score)
                    add(
                        ParsedLocalRecommendationItem(
                            id = id,
                            name = name,
                            description = desc,
                            category = category,
                            rating = rating,
                            priceLabel = priceLabel,
                        ),
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun priceTierFromScore(score: Float): String = when {
        score >= 0.75f -> "$$$"
        score >= 0.45f -> "$$"
        else -> "$"
    }
}
