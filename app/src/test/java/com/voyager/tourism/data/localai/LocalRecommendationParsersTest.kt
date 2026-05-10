package com.voyager.tourism.data.localai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun parseItems_nestedDataObject() {
        val json = """{"data":{"items":[{"id":"a","name":"N","score":0.8}]}}"""
        val list = LocalRecommendationParsers.parseItems(json)
        assertEquals(1, list.size)
        assertEquals("$$$", list[0].priceLabel)
        assertEquals(4f, list[0].rating, 0.01f)
    }

    @Test
    fun parseItems_recommendationsKey_usesSimilarityAndMidTier() {
        val json = """{"recommendations":[{"id":"z","similarity":0.5}]}"""
        val list = LocalRecommendationParsers.parseItems(json)
        assertEquals(1, list.size)
        assertEquals("$$", list[0].priceLabel)
    }

    @Test
    fun parseItems_lowScore_dollarTier() {
        val json = """{"items":[{"id":"l","score":0.1}]}"""
        val list = LocalRecommendationParsers.parseItems(json)
        assertEquals("$", list[0].priceLabel)
        assertEquals(0.5f, list[0].rating, 0.01f)
    }

    @Test
    fun parseItems_skipsBlankId() {
        val json = """{"items":[{"id":"","name":"ghost"}]}"""
        assertTrue(LocalRecommendationParsers.parseItems(json).isEmpty())
    }

    @Test
    fun parseItems_nameFallsBackToId() {
        val json = """{"items":[{"id":"only-id","category":""}]}"""
        val list = LocalRecommendationParsers.parseItems(json)
        assertEquals("only-id", list[0].name)
        assertEquals("general", list[0].category)
    }
}
