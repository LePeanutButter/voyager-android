package com.voyager.tourism.data.localai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalChatHistoryParsersTest {

    @Test
    fun parseMessages_blank_returnsEmpty() {
        assertTrue(LocalChatHistoryParsers.parseMessages("   ").isEmpty())
    }

    @Test
    fun parseMessages_invalidJson_returnsEmpty() {
        assertTrue(LocalChatHistoryParsers.parseMessages("{").isEmpty())
    }

    @Test
    fun parseMessages_missingMessagesArray_returnsEmpty() {
        assertTrue(LocalChatHistoryParsers.parseMessages("""{"x":[]}""").isEmpty())
    }

    @Test
    fun parseMessages_skipsNonObjectEntries() {
        val json = """{"messages":[null,42,{"role":"user","content":"hola"}]}"""
        val rows = LocalChatHistoryParsers.parseMessages(json)
        assertEquals(listOf(true to "hola"), rows)
    }

    @Test
    fun parseMessages_usesMessageWhenContentBlank() {
        val json = """{"messages":[{"role":"ASSISTANT","content":"","message":"  ok  "}]}"""
        val rows = LocalChatHistoryParsers.parseMessages(json)
        assertEquals(listOf(false to "  ok  "), rows)
    }

    @Test
    fun parseMessages_skipsBlankText() {
        val json = """{"messages":[{"role":"user","content":""}]}"""
        assertTrue(LocalChatHistoryParsers.parseMessages(json).isEmpty())
    }
}
