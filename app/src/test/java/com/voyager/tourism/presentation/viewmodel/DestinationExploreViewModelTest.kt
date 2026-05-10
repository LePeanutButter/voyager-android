package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.CatalogRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
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
        coEvery { prefs.getCurrentUserId() } returns "1"
        coEvery {
            catalog.activities(any(), any(), any(), any())
        } returns Response.success(
            """{"data":{"activities":[]}}""".toResponseBody("application/json".toMediaType()),
        )
    }

    @Test
    fun `loadExplore fills destination label`() = runBlocking {
        vm.loadExplore("Paris", "FR", "dest1")

        // La etiqueta se asigna al inicio del launch; el catálogo va por Dispatchers.IO.
        val label = withTimeout(5_000) {
            vm.destinationLabel.first { it.isNotBlank() }
        }
        assertTrue(label.contains("Paris"))
    }
}
