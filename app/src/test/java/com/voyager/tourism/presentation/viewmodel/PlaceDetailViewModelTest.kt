package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.AiTravelerMatchDto
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class PlaceDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>()
    private lateinit var vm: PlaceDetailViewModel

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        every { Dispatchers.Main } returns mainDispatcherRule.dispatcher
        every { Dispatchers.Default } returns mainDispatcherRule.dispatcher
        
        clearMocks(voyagerAi, prefs)

        every { prefs.getCurrentUserId() } returns "42"
        vm = PlaceDetailViewModel(voyagerAi, prefs)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
    }


    @Test
    fun `loadPlace success clears loading`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            AiMatchingResponseDto(
                matches = listOf(AiTravelerMatchDto("m1", "Item 1", 30, 85.0, emptyList(), 0.85, "Bio")),
                userId = "42",
                totalMatches = 1
            )
        )

        vm.loadPlace("lima_peru")
        advanceUntilIdle()

        assertFalse(vm.isLoading.value)
        assertTrue(vm.rankedItems.value.isNotEmpty())
    }

    @Test
    fun `loadPlace http error sets error`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.error(
            502,
            "bad".toResponseBody("text/plain".toMediaType()),
        )
        vm.loadPlace("lima_peru")
        advanceUntilIdle()

        assertFalse(vm.isLoading.value)
        assertEquals("HTTP 502", vm.error.value)
        assertTrue(vm.rankedItems.value.isEmpty())
    }

    @Test
    fun `loadPlace uses anonymous when user id blank`() = runTest {
        every { prefs.getCurrentUserId() } returns "   "
        val bodySlot = slot<LocalRecommendationRequestBody>()
        coEvery { voyagerAi.postLocalRecommendations(capture(bodySlot)) } returns Response.success(
            AiMatchingResponseDto(emptyList(), "anonymous", 0)
        )
        vm.loadPlace("x")
        advanceUntilIdle()

        coVerify(atLeast = 1) { voyagerAi.postLocalRecommendations(any()) }
        assertEquals("anonymous", bodySlot.captured.userId)
    }

    @Test
    fun `loadPlace exception maps message`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } throws RuntimeException("boom")
        vm.loadPlace("p")
        advanceUntilIdle()
        assertEquals("boom", vm.error.value)
    }

    @Test
    fun `loadPlace exception null message uses default`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } throws RuntimeException()
        vm.loadPlace("p")
        advanceUntilIdle()
        assertEquals("Error de red", vm.error.value)
    }
}

