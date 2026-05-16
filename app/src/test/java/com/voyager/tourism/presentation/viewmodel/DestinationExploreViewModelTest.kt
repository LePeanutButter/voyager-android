package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.ActivityDto
import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.AiTravelerMatchDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.CatalogRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class DestinationExploreViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val catalog = mockk<CatalogRepository>(relaxed = true)
    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>(relaxed = true)
    private val dispatchers = TestDispatcherProvider(testDispatcher)
    private lateinit var vm: DestinationExploreViewModel

    private fun <T> ok(data: T) = ApiResponse("t", 200, "OK", data, null, null)

    @Before
    fun setup() {
        vm = DestinationExploreViewModel(catalog, voyagerAi, prefs, dispatchers)
        every { prefs.getCurrentUserId() } returns "1"
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns ok(emptyList())
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `loadExplore fills destination label`() = runTest {
        vm.loadExplore("Paris", "FR", "dest1")
        advanceUntilIdle()
        assertEquals("Paris, FR", vm.destinationLabel.value)
    }

    @Test
    fun `loadExplore catalog http error sets catalogError`() = runTest {
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } throws RuntimeException("Error")
        vm.loadExplore("Lima", "PE", "")
        advanceUntilIdle()
        assertNotNull(vm.catalogError.value)
        assertTrue(vm.activities.value.isEmpty())
    }

    @Test
    fun `rankCatalog without activities sets error`() = runTest {
        vm.rankCatalog()
        advanceUntilIdle()
        assertTrue(vm.rankError.value?.contains("catálogo") == true)
    }

    @Test
    fun `rankCatalog without user sets error`() = runTest {
        every { prefs.getCurrentUserId() } returns "1"
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns ok(listOf(ActivityDto("1", "Museum", pictures = emptyList())))
        vm.loadExplore("X", "Y", "")
        advanceUntilIdle()
        
        every { prefs.getCurrentUserId() } returns null
        vm.rankCatalog()
        advanceUntilIdle()
        assertTrue(vm.rankError.value?.contains("sesión") == true)
    }

    @Test
    fun `rankCatalog success parses ranked items`() = runTest {
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns ok(listOf(ActivityDto("1", "Walk", "Nice", pictures = emptyList())))
        
        val body = mockk<AiMatchingResponseDto>()
        val json = """{"items":[{"id":"1","name":"Walk","category":"catalog_activity","score":0.5,"content_text":"Nice"}]}"""
        every { body.toString() } returns json
        
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(body)

        vm.loadExplore("Paris", "FR", "d1")
        advanceUntilIdle()
        
        vm.rankCatalog()
        advanceUntilIdle()

        coVerify(atLeast = 1) { voyagerAi.postLocalRecommendations(any()) }
        assertTrue(vm.ranked.value.isNotEmpty())
        assertEquals("Walk", vm.ranked.value.first().name)
    }

    @Test
    fun `rankCatalog http error sets rankError from body`() = runTest {
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns ok(listOf(ActivityDto("1", "Walk", pictures = emptyList())))
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.error(
            503,
            "rank-down".toResponseBody("text/plain".toMediaType()),
        )
        vm.loadExplore("Paris", "FR", "")
        advanceUntilIdle()
        vm.rankCatalog()
        advanceUntilIdle()
        assertEquals("rank-down", vm.rankError.value)
    }

    @Test
    fun `rankCatalog http error with empty body yields empty rankError`() = runTest {
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns ok(listOf(ActivityDto("1", "Walk", pictures = emptyList())))
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.error(
            502,
            "".toResponseBody(null),
        )
        vm.loadExplore("Paris", "FR", "")
        advanceUntilIdle()
        vm.rankCatalog()
        advanceUntilIdle()
        assertEquals("HTTP 502", vm.rankError.value)
    }

    @Test
    fun `rankCatalog exception uses default when message null`() = runTest {
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns ok(listOf(ActivityDto("1", "Walk", pictures = emptyList())))
        coEvery { voyagerAi.postLocalRecommendations(any()) } throws RuntimeException()
        vm.loadExplore("Paris", "FR", "")
        advanceUntilIdle()
        vm.rankCatalog()
        advanceUntilIdle()
        assertEquals("Error de red", vm.rankError.value)
    }
}
