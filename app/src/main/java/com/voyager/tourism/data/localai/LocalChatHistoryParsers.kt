package com.voyager.tourism.data.localai

import org.json.JSONArray
import org.json.JSONObject

/**
 * Historial [GET /local/chat/history/{session_id}] — mensajes con role user/assistant y content.
 */
object LocalChatHistoryParsers {

    fun parseMessages(json: String): List<Pair<Boolean, String>> {
        val raw = json.trim()
        if (raw.isEmpty()) return emptyList()
        return try {
            val root = JSONObject(raw)
            val arr = root.optJSONArray("messages") ?: return emptyList()
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val role = o.optString("role").lowercase()
                    val isUser = role == "user"
                    val text = o.optString("content").ifBlank { o.optString("message") }
                    if (text.isNotBlank()) add(isUser to text)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
