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
class PreferencesManagerTest {

    @Test
    fun `save and read user id`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pm = PreferencesManager(ctx)
        pm.saveCurrentUserId("42")
        assertEquals("42", pm.getCurrentUserId())
    }

    @Test
    fun `clearAuthData removes user id`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pm = PreferencesManager(ctx)
        pm.saveCurrentUserId("1")
        pm.saveAuthToken("tok")
        pm.clearAuthData()
        assertNull(pm.getCurrentUserId())
        assertNull(pm.getAuthToken())
    }
}
