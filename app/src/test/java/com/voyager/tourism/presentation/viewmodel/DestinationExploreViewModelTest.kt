package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.CatalogRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class DestinationExploreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val catalog = mockk<CatalogRepository>(relaxed = true)
    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>(relaxed = true)
    private lateinit var vm: DestinationExploreViewModel

    @Before
    fun setup() {
        vm = DestinationExploreViewModel(catalog, voyagerAi, prefs)
        every { prefs.getCurrentUserId() } returns "1"
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns Response.success(
            """{"data":{"activities":[]}}""".toResponseBody("application/json".toMediaType()),
        )
    }

    @Test
    fun `loadExplore fills destination label`() = runBlocking {
        vm.loadExplore("Paris", "FR", "dest1")
        // MainDispatcherRule usa UnconfinedTestDispatcher: el launch llega hasta el primer suspend (IO)
        // después de asignar la etiqueta; un yield asegura esa ejecución en otros schedulers.
        yield()

        assertEquals("Paris, FR", vm.destinationLabel.value)
    }

    @Test
    fun `loadExplore catalog http error sets catalogError`() = runBlocking {
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns Response.error(500, "x".toResponseBody("text/plain".toMediaType()))
        vm.loadExplore("Lima", "PE", "")
        val deadline = System.currentTimeMillis() + 15_000
        while (vm.catalogError.value == null && System.currentTimeMillis() < deadline) {
            delay(20)
        }
        assertNotNull(vm.catalogError.value)
        assertTrue(vm.activities.value.isEmpty())
    }

    @Test
    fun `rankCatalog without activities sets error`() = runBlocking {
        vm.rankCatalog()
        delay(400)
        assertTrue(vm.rankError.value?.contains("catálogo") == true)
    }

    @Test
    fun `rankCatalog without user sets error`() = runBlocking {
        every { prefs.getCurrentUserId() } returns "1"
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns Response.success(
            """{"data":[{"id":"1","name":"Museum"}]}""".toResponseBody("application/json".toMediaType()),
        )
        vm.loadExplore("X", "Y", "")
        val loaded = System.currentTimeMillis() + 15_000
        while (vm.activities.value.isEmpty() && System.currentTimeMillis() < loaded) {
            delay(25)
        }
        every { prefs.getCurrentUserId() } returns null
        vm.rankCatalog()
        delay(600)
        assertTrue(vm.rankError.value?.contains("sesión") == true)
    }

    @Test
    fun `rankCatalog success parses ranked items`() = runBlocking {
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns Response.success(
            """{"data":[{"id":"1","name":"Walk","shortDescription":"Nice"}]}"""
                .toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            """{"items":[{"id":"1","name":"Walk","category":"c","score":0.5,"description":"d"}]}"""
                .toResponseBody("application/json".toMediaType()),
        )
        vm.loadExplore("Paris", "FR", "d1")
        val loaded = System.currentTimeMillis() + 15_000
        while (vm.activities.value.isEmpty() && System.currentTimeMillis() < loaded) {
            delay(25)
        }
        vm.rankCatalog()
        delay(800)
        coVerify(atLeast = 1) { voyagerAi.postLocalRecommendations(any()) }
        assertTrue(vm.ranked.value.isNotEmpty())
    }
}
