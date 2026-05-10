package com.voyager.tourism.data.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogActivitiesParserTest {

    @Test
    fun `parseActivitiesJson returns empty for blank`() {
        assertTrue(CatalogActivitiesParser.parseActivitiesJson("   ").isEmpty())
    }

    @Test
    fun `parseActivitiesJson returns empty for invalid json`() {
        assertTrue(CatalogActivitiesParser.parseActivitiesJson("{").isEmpty())
    }

    @Test
    fun `parseActivitiesJson handles top-level data array`() {
        val json = """{"data":[{"id":"1","name":"Walk","shortDescription":"Nice"}]}"""
        val rows = CatalogActivitiesParser.parseActivitiesJson(json)
        assertEquals(1, rows.size)
        assertEquals("1", rows[0].id)
        assertEquals("Walk", rows[0].name)
        assertEquals("Nice", rows[0].description)
    }

    @Test
    fun `parseActivitiesJson reads nested activities under data object`() {
        val json = """{"data":{"activities":[{"name":"Museum","description":"Art"}]}}"""
        val rows = CatalogActivitiesParser.parseActivitiesJson(json)
        assertEquals(1, rows.size)
        assertEquals("Museum", rows[0].name)
    }

    @Test
    fun `parseActivitiesJson reads items key`() {
        val json = """{"data":{"items":[{"name":"Food","shortDescription":"Tasty"}]}}"""
        val rows = CatalogActivitiesParser.parseActivitiesJson(json)
        assertEquals(1, rows.size)
        assertEquals("Food", rows[0].name)
    }

    @Test
    fun `parseActivitiesJson reads nested data array`() {
        val json = """{"data":{"data":[{"name":"X","title":"","description":"d"}]}}"""
        val rows = CatalogActivitiesParser.parseActivitiesJson(json)
        assertEquals(1, rows.size)
        assertEquals("X", rows[0].name)
    }

    @Test
    fun `parseActivitiesJson uses title when name blank`() {
        val json = """{"data":[{"title":"OnlyTitle","description":"d"}]}"""
        val rows = CatalogActivitiesParser.parseActivitiesJson(json)
        assertEquals(1, rows.size)
        assertEquals("OnlyTitle", rows[0].name)
    }

    @Test
    fun `parseActivitiesJson skips rows without usable name`() {
        val json = """{"data":[{"description":"no name"}]}"""
        assertTrue(CatalogActivitiesParser.parseActivitiesJson(json).isEmpty())
    }

    @Test
    fun `parseActivitiesJson caps at 14 rows`() {
        val items = (1..20).joinToString(",") { i ->
            """{"id":"$i","name":"Act$i"}"""
        }
        val json = """{"data":[$items]}"""
        assertEquals(14, CatalogActivitiesParser.parseActivitiesJson(json).size)
    }

    @Test
    fun `parseActivitiesJson generates id when missing`() {
        val json = """{"data":[{"name":"OnlyName"}]}"""
        val rows = CatalogActivitiesParser.parseActivitiesJson(json)
        assertEquals(1, rows.size)
        assertTrue(rows[0].id.startsWith("act-OnlyName"))
    }
}
