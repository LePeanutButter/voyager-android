package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.AiTravelerMatchDto
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.dto.LocalRecommendationResponseDto
import com.voyager.tourism.data.dto.LocalRecommendationItemDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestDispatcherProvider
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>()
    private val dispatchers = TestDispatcherProvider(testDispatcher)
    private lateinit var vm: PlaceDetailViewModel

    @Before
    fun setup() {
        clearMocks(voyagerAi, prefs)

        every { prefs.getCurrentUserId() } returns "42"
        vm = PlaceDetailViewModel(voyagerAi, prefs, dispatchers)
    }

    @After
    fun tearDown() {
    }


    @Test
    fun `loadPlace success clears loading`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            LocalRecommendationResponseDto(
                items = listOf(
                    LocalRecommendationItemDto(
                        id = "m1",
                        name = "Item 1",
                        category = "match",
                        score = 0.85,
                        contentText = "Bio"
                    )
                )
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
            LocalRecommendationResponseDto(items = emptyList())
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

