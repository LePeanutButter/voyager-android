package com.voyager.tourism.data.local

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TokenManagerTest {

    @Test
    fun `delegates to preferences`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(ctx)
        val tm = TokenManager(prefs)
        tm.saveToken("jwt")
        assertEquals("jwt", tm.getToken())
        tm.saveUser("""{"id":1}""")
        assertEquals("""{"id":1}""", tm.getUser())
        tm.clear()
        assertNull(tm.getToken())
        assertNull(tm.getUser())
    }
}
