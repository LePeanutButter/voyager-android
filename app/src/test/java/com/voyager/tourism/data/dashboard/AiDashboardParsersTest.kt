package com.voyager.tourism.data.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiDashboardParsersTest {

    @Test
    fun parseTrendsDashboard_snakeCase_emerging_destinations() {
        val json = """
            {"data":{"emerging_destinations":[
              {"destination_id":"d1","name":"Bariloche","country":"AR"},
              {"name":"Mendoza","country":"AR"}
            ]}}
        """.trimIndent()
        val list = AiDashboardParsers.parseTrendsDashboard(json)
        assertEquals(2, list.size)
        assertEquals("d1", list[0].id)
        assertEquals("Bariloche", list[0].name)
        assertEquals("AR", list[0].country)
        assertEquals(null, list[1].id)
        assertEquals("Mendoza", list[1].name)
    }

    @Test
    fun parseTrendsDashboard_camelCase_wrappedInData() {
        val json = """
            {"data":{"emergingDestinations":[
              {"destinationId":"x","name":"Tokyo","country":"JP"}
            ]}}
        """.trimIndent()
        val list = AiDashboardParsers.parseTrendsDashboard(json)
        assertEquals(1, list.size)
        assertEquals("x", list[0].id)
        assertEquals("Tokyo", list[0].name)
    }

    @Test
    fun parseTrendsDashboard_limitsToThree() {
        val items = (1..10).joinToString(",") { i ->
            """{"name":"City$i","country":"C"}"""
        }
        val json = """{"emerging_destinations":[$items]}"""
        assertEquals(3, AiDashboardParsers.parseTrendsDashboard(json).size)
    }

    @Test
    fun parseWeeklyDigestLines_micro_trends() {
        val json = """
            {"data":{"micro_trends":[
              {"title":"Más búsquedas en Patagonia"},
              {"headline":"Solo headline"}
            ]}}
        """.trimIndent()
        val lines = AiDashboardParsers.parseWeeklyDigestLines(json)
        assertEquals(2, lines.size)
        assertEquals("Más búsquedas en Patagonia", lines[0])
        assertEquals("Solo headline", lines[1])
    }

    @Test
    fun parseWeeklyDigestLines_geoFallback() {
        val json = """
            {"microTrends":[{"geo":{"name":"Ushuaia"}}]}
        """.trimIndent()
        val lines = AiDashboardParsers.parseWeeklyDigestLines(json)
        assertEquals(listOf("Ushuaia"), lines)
    }

    @Test
    fun parseSeasonalityLines_destinations() {
        val json = """
            {"destinations":[
              {"name":"El Calafate"},
              {"destination_name":"Salta"}
            ]}
        """.trimIndent()
        val lines = AiDashboardParsers.parseSeasonalityLines(json)
        assertEquals(listOf("El Calafate", "Salta"), lines)
    }

    @Test
    fun parseBlank_returnsEmpty() {
        assertTrue(AiDashboardParsers.parseTrendsDashboard("").isEmpty())
        assertTrue(AiDashboardParsers.parseWeeklyDigestLines("   ").isEmpty())
        assertTrue(AiDashboardParsers.parseSeasonalityLines("{}").isEmpty())
    }

    @Test
    fun parseWeeklyDigestLines_includesSubtitleWhenPresent() {
        val json = """
            {"micro_trends":[{"title":"T","summary":"S"}]}
        """.trimIndent()
        val lines = AiDashboardParsers.parseWeeklyDigestLines(json)
        assertEquals(listOf("T — S"), lines)
    }

    @Test
    fun parseWeeklyDigestRows_trendsKey_andSignalSubtitle() {
        val json = """{"trends":[{"headline":"H","signal":"up"}]}"""
        val rows = AiDashboardParsers.parseWeeklyDigestRows(json)
        assertEquals(1, rows.size)
        assertEquals("H", rows[0].title)
        assertEquals("up", rows[0].subtitle)
    }

    @Test
    fun parseSeasonalityRows_profilesKey_andDefaultSubtitle() {
        val json = """{"profiles":[{"destination":"Ushuaia","note":""}]}"""
        val rows = AiDashboardParsers.parseSeasonalityRows(json)
        assertEquals(1, rows.size)
        assertEquals("Ushuaia", rows[0].title)
        assertTrue(rows[0].subtitle.isNotBlank())
    }

    @Test
    fun parseSeasonalityRows_destinationNameField() {
        val json = """{"destinations":[{"destination_name":"Salta"}]}"""
        assertEquals(listOf("Salta"), AiDashboardParsers.parseSeasonalityLines(json))
    }

    @Test
    fun parseTrendsDashboard_topLevelEmergingWithoutDataWrapper() {
        val json = """{"emerging_destinations":[{"name":"P","country":"Q"}]}"""
        val list = AiDashboardParsers.parseTrendsDashboard(json)
        assertEquals(1, list.size)
        assertEquals("P", list[0].name)
        assertEquals("Q", list[0].country)
    }
}
