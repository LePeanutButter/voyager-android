package com.voyager.tourism.data.session

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionInvalidationNotifierTest {

    @Test
    fun `notify emits unit`() = runBlocking {
        val notifier = SessionInvalidationNotifier()
        val first = async { notifier.events.first() }
        yield()
        notifier.notifySessionExpired()
        assertEquals(Unit, first.await())
    }
}
