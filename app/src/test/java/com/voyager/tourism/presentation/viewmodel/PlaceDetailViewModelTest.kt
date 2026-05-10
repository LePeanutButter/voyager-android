package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class PlaceDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>()
    private lateinit var vm: PlaceDetailViewModel

    @Before
    fun setup() {
        every { prefs.getCurrentUserId() } returns "42"
        vm = PlaceDetailViewModel(voyagerAi, prefs)
    }

    @Test
    fun `loadPlace success clears loading`() = runBlocking {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            """{"items":[{"id":"1","name":"Tour","category":"x","score":0.9,"description":"d"}]}"""
                .toResponseBody("application/json".toMediaType()),
        )

        vm.loadPlace("lima_peru")

        val deadline = System.currentTimeMillis() + 60_000
        while (vm.isLoading.value && System.currentTimeMillis() < deadline) {
            delay(10)
        }

        assertFalse(vm.isLoading.value)
        assertTrue(vm.rankedItems.value.isNotEmpty())
    }

    @Test
    fun `loadPlace http error sets error`() = runBlocking {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.error(
            502,
            "bad".toResponseBody("text/plain".toMediaType()),
        )
        vm.loadPlace("lima_peru")
        delay(800)
        assertFalse(vm.isLoading.value)
        assertEquals("bad", vm.error.value)
        assertTrue(vm.rankedItems.value.isEmpty())
    }

    @Test
    fun `loadPlace uses anonymous when user id blank`() = runBlocking {
        every { prefs.getCurrentUserId() } returns "   "
        val bodySlot = slot<LocalRecommendationRequestBody>()
        coEvery { voyagerAi.postLocalRecommendations(capture(bodySlot)) } returns Response.success(
            """{"items":[{"id":"1","name":"Tour","category":"x","score":0.9,"description":"d"}]}"""
                .toResponseBody("application/json".toMediaType()),
        )
        vm.loadPlace("x")
        delay(800)
        coVerify(exactly = 1) { voyagerAi.postLocalRecommendations(any()) }
        assertEquals("anonymous", bodySlot.captured.userId)
    }

    @Test
    fun `loadPlace exception maps message`() = runBlocking {
        coEvery { voyagerAi.postLocalRecommendations(any()) } throws RuntimeException("boom")
        vm.loadPlace("p")
        delay(800)
        assertEquals("boom", vm.error.value)
    }

    @Test
    fun `loadPlace exception null message uses default`() = runBlocking {
        coEvery { voyagerAi.postLocalRecommendations(any()) } throws RuntimeException()
        vm.loadPlace("p")
        delay(800)
        assertEquals("Error de red", vm.error.value)
    }
}
