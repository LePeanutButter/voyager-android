package com.voyager.tourism.data.localai

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalRecommendationParsersTest {

    @Test
    fun parseItems_extractsIdNameScore() {
        val json = """
            {"items":[
              {"id":"x","name":"Museo","category":"cultural","similarity":0.8,"content_text":"Arte"},
              {"id":"y","name":"Parque","category":"nature","score":0.4}
            ]}
        """.trimIndent()
        val list = LocalRecommendationParsers.parseItems(json)
        assertEquals(2, list.size)
        assertEquals("x", list[0].id)
        assertEquals("Museo", list[0].name)
        assertEquals(4f, list[0].rating, 0.01f)
        assertEquals("\$\$\$", list[0].priceLabel)
        assertEquals(2f, list[1].rating, 0.01f)
    }

    @Test
    fun parseItems_emptyArray() {
        assertEquals(0, LocalRecommendationParsers.parseItems("""{"items":[]}""").size)
    }
}
