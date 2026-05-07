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

    @Test
    fun `saveRefreshToken stores and clears on null or blank`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pm = PreferencesManager(ctx)
        pm.saveRefreshToken("refresh-1")
        assertEquals("refresh-1", pm.getRefreshToken())
        pm.saveRefreshToken(null)
        assertNull(pm.getRefreshToken())
        pm.saveRefreshToken("r2")
        pm.saveRefreshToken("   ")
        assertNull(pm.getRefreshToken())
    }

    @Test
    fun `saveCurrentUserId blank clears id`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pm = PreferencesManager(ctx)
        pm.saveCurrentUserId("7")
        assertEquals("7", pm.getCurrentUserId())
        pm.saveCurrentUserId(" ")
        assertNull(pm.getCurrentUserId())
    }

    @Test
    fun `saveUserJson round trip and blank clears`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pm = PreferencesManager(ctx)
        pm.saveUserJson("""{"id":"1"}""")
        assertEquals("""{"id":"1"}""", pm.getUserJson())
        pm.saveUserJson(" ")
        assertNull(pm.getUserJson())
    }

    @Test
    fun `authTokenFlow mirrors token writes and clearAuthData`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val pm = PreferencesManager(ctx)
        assertNull(pm.authTokenFlow.value)
        pm.saveAuthToken("jwt")
        assertEquals("jwt", pm.authTokenFlow.value)
        pm.clearAuthData()
        assertNull(pm.authTokenFlow.value)
    }
}
