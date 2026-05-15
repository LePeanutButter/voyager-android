package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.SmarTripSettingsPayload
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val prefs = mockk<PreferencesManager>(relaxed = true)
    private var stored = SmarTripSettingsPayload()
    private lateinit var vm: SettingsViewModel

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        stored = SmarTripSettingsPayload()
        every { prefs.getSmarTripSettings() } answers { stored }
        every { prefs.updateSmarTripSettings(any()) } answers {
            val transform = firstArg<(SmarTripSettingsPayload) -> SmarTripSettingsPayload>()
            stored = transform(stored)
        }
        vm = SettingsViewModel(prefs)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
    }

    @Test
    fun `initial state mirrors preferences`() {
        stored = SmarTripSettingsPayload(darkMode = true, profileVisibility = "private")
        vm = SettingsViewModel(prefs)
        assertTrue(vm.state.value.darkMode)
        assertEquals("private", vm.state.value.profileVisibility)
    }

    @Test
    fun `refresh reloads from preferences`() {
        stored = SmarTripSettingsPayload(darkMode = false)
        vm.refresh()
        assertFalse(vm.state.value.darkMode)
    }

    @Test
    fun `setDarkMode updates preferences and state`() {
        vm.setDarkMode(true)
        assertTrue(stored.darkMode)
        assertTrue(vm.state.value.darkMode)
        verify(atLeast = 1) { prefs.updateSmarTripSettings(any()) }
    }

    @Test
    fun `setCommunitySuggestions updates stored payload`() {
        vm.setCommunitySuggestions(false)
        assertFalse(stored.communitySuggestions)
        assertFalse(vm.state.value.communitySuggestions)
    }

    @Test
    fun `setProfileVisibility passes value through`() {
        vm.setProfileVisibility("only_me")
        assertEquals("only_me", stored.profileVisibility)
        assertEquals("only_me", vm.state.value.profileVisibility)
    }
}
