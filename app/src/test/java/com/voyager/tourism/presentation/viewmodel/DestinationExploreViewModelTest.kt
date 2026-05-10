package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.catalog.CatalogActivityRow
import com.voyager.tourism.data.catalog.CatalogActivitiesParser
import com.voyager.tourism.data.destination.buildDestinationExploreLabel
import com.voyager.tourism.data.destination.resolveDestinationHint
import com.voyager.tourism.data.dto.LocalRecommendationCandidateBody
import com.voyager.tourism.data.dto.LocalRecommendationRequestBody
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.localai.LocalRecommendationParsers
import com.voyager.tourism.data.localai.ParsedLocalRecommendationItem
import com.voyager.tourism.domain.repository.CatalogRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class DestinationExploreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val catalogRepository = mockk<CatalogRepository>()
    private val voyagerAiRepository = mockk<VoyagerAiRepository>()
    private val preferencesManager = mockk<PreferencesManager>()
    private lateinit var vm: DestinationExploreViewModel
    private val testScope = TestScope()

    @Before
    fun setup() {
        vm = DestinationExploreViewModel(catalogRepository, voyagerAiRepository, preferencesManager)
    }

    @Test
    fun `initial state is empty`() {
        assertEquals("", vm.destinationLabel.value)
        assertEquals("", vm.catalogDestId.value)
        assertFalse(vm.catalogLoading.value)
        assertNull(vm.catalogError.value)
        assertTrue(vm.activities.value.isEmpty())
        assertFalse(vm.rankLoading.value)
        assertNull(vm.rankError.value)
        assertTrue(vm.ranked.value.isEmpty())
    }

    @Test
    fun `loadExplore success`() = runBlocking {
        val mockResponse = Response.success(
            """{"activities": [{"id": "1", "name": "Eiffel Tower", "category": "attraction", "description": "Famous tower"}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { catalogRepository.activities(any(), any(), any(), any()) } returns mockResponse

        vm.loadExplore("Paris", "France", "paris-fr")
        testScope.advanceUntilIdle()

        assertEquals("Paris, Francia", vm.destinationLabel.value)
        assertEquals("paris-fr", vm.catalogDestId.value)
        assertFalse(vm.catalogLoading.value)
        assertNull(vm.catalogError.value)
        assertEquals(1, vm.activities.value.size)
        assertEquals("Eiffel Tower", vm.activities.value[0].name)
        assertEquals("attraction", vm.activities.value[0].category)
        assertEquals("Famous tower", vm.activities.value[0].description)
    }

    @Test
    fun `loadExplore with HTTP error`() = runBlocking {
        coEvery { catalogRepository.activities(any(), any(), any(), any()) } returns Response.error(
            500,
            "Server error".toResponseBody(null),
        )

        vm.loadExplore("Paris", "France", "paris-fr")
        testScope.advanceUntilIdle()

        assertEquals("Paris, Francia", vm.destinationLabel.value)
        assertEquals("paris-fr", vm.catalogDestId.value)
        assertFalse(vm.catalogLoading.value)
        assertEquals("Server error", vm.catalogError.value)
        assertTrue(vm.activities.value.isEmpty())
    }

    @Test
    fun `loadExplore with network exception`() = runBlocking {
        coEvery { catalogRepository.activities(any(), any(), any(), any()) } throws RuntimeException("Network error")

        vm.loadExplore("Paris", "France", "paris-fr")
        testScope.advanceUntilIdle()

        assertEquals("Paris, Francia", vm.destinationLabel.value)
        assertEquals("paris-fr", vm.catalogDestId.value)
        assertFalse(vm.catalogLoading.value)
        assertEquals("Error de catálogo", vm.catalogError.value)
        assertTrue(vm.activities.value.isEmpty())
    }

    @Test
    fun `loadExplore with empty response`() = runBlocking {
        val mockResponse = Response.success(
            """{"activities": []}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { catalogRepository.activities(any(), any(), any(), any()) } returns mockResponse

        vm.loadExplore("Paris", "France", "paris-fr")
        testScope.advanceUntilIdle()

        assertEquals("Paris, Francia", vm.destinationLabel.value)
        assertEquals("paris-fr", vm.catalogDestId.value)
        assertFalse(vm.catalogLoading.value)
        assertNull(vm.catalogError.value)
        assertTrue(vm.activities.value.isEmpty())
    }

    @Test
    fun `loadExplore with malformed JSON`() = runBlocking {
        val mockResponse = Response.success(
            """{"invalid": "json"}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { catalogRepository.activities(any(), any(), any(), any()) } returns mockResponse

        vm.loadExplore("Paris", "France", "paris-fr")
        testScope.advanceUntilIdle()

        assertEquals("Paris, Francia", vm.destinationLabel.value)
        assertEquals("paris-fr", vm.catalogDestId.value)
        assertFalse(vm.catalogLoading.value)
        assertNull(vm.catalogError.value)
        assertTrue(vm.activities.value.isEmpty())
    }

    @Test
    fun `loadExplore with whitespace destination ID`() = runBlocking {
        val mockResponse = Response.success(
            """{"activities": []}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { catalogRepository.activities(any(), any(), any(), any()) } returns mockResponse

        vm.loadExplore("Paris", "France", "  paris-fr  ")
        testScope.advanceUntilIdle()

        assertEquals("Paris, Francia", vm.destinationLabel.value)
        assertEquals("paris-fr", vm.catalogDestId.value)
        assertFalse(vm.catalogLoading.value)
        assertNull(vm.catalogError.value)
    }

    @Test
    fun `rankCatalog success`() = runBlocking {
        // Setup activities first
        val activities = listOf(
            CatalogActivityRow(
                id = "1",
                name = "Eiffel Tower",
                category = "attraction",
                description = "Famous tower"
            ),
            CatalogActivityRow(
                id = "2",
                name = "Louvre Museum",
                category = "museum",
                description = "Art museum"
            )
        )
        
        every { preferencesManager.getCurrentUserId() } returns "user123"
        
        val mockResponse = Response.success(
            """{"items": [{"name": "Eiffel Tower", "type": "attraction"}, {"name": "Louvre Museum", "type": "museum"}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAiRepository.postLocalRecommendations(any()) } returns mockResponse

        // Set activities manually for testing
        vm._activities.value = activities
        vm._destinationLabel.value = "Paris, Francia"

        vm.rankCatalog()
        testScope.advanceUntilIdle()

        assertFalse(vm.rankLoading.value)
        assertNull(vm.rankError.value)
        assertEquals(2, vm.ranked.value.size)
        assertEquals("Eiffel Tower", vm.ranked.value[0].name)
        assertEquals("attraction", vm.ranked.value[0].type)
        assertEquals("Louvre Museum", vm.ranked.value[1].name)
        assertEquals("museum", vm.ranked.value[1].type)
    }

    @Test
    fun `rankCatalog with empty activities`() = runBlocking {
        vm._activities.value = emptyList()

        vm.rankCatalog()
        testScope.advanceUntilIdle()

        assertFalse(vm.rankLoading.value)
        assertEquals("Carga el catálogo antes de rankear.", vm.rankError.value)
        assertTrue(vm.ranked.value.isEmpty())
    }

    @Test
    fun `rankCatalog with no user ID`() = runBlocking {
        every { preferencesManager.getCurrentUserId() } returns null
        
        val activities = listOf(
            CatalogActivityRow(id = "1", name = "Eiffel Tower", category = "attraction", description = "Famous tower")
        )
        vm._activities.value = activities

        vm.rankCatalog()
        testScope.advanceUntilIdle()

        assertFalse(vm.rankLoading.value)
        assertEquals("Inicia sesión para obtener recomendaciones priorizadas con IA.", vm.rankError.value)
        assertTrue(vm.ranked.value.isEmpty())
    }

    @Test
    fun `rankCatalog with blank user ID`() = runBlocking {
        every { preferencesManager.getCurrentUserId() } returns ""
        
        val activities = listOf(
            CatalogActivityRow(id = "1", name = "Eiffel Tower", category = "attraction", description = "Famous tower")
        )
        vm._activities.value = activities

        vm.rankCatalog()
        testScope.advanceUntilIdle()

        assertFalse(vm.rankLoading.value)
        assertEquals("Inicia sesión para obtener recomendaciones priorizadas con IA.", vm.rankError.value)
        assertTrue(vm.ranked.value.isEmpty())
    }

    @Test
    fun `rankCatalog with HTTP error`() = runBlocking {
        every { preferencesManager.getCurrentUserId() } returns "user123"
        
        val activities = listOf(
            CatalogActivityRow(id = "1", name = "Eiffel Tower", category = "attraction", description = "Famous tower")
        )
        vm._activities.value = activities
        vm._destinationLabel.value = "Paris, Francia"

        coEvery { voyagerAiRepository.postLocalRecommendations(any()) } returns Response.error(
            500,
            "Server error".toResponseBody(null),
        )

        vm.rankCatalog()
        testScope.advanceUntilIdle()

        assertFalse(vm.rankLoading.value)
        assertEquals("Server error", vm.rankError.value)
        assertTrue(vm.ranked.value.isEmpty())
    }

    @Test
    fun `rankCatalog with network exception`() = runBlocking {
        every { preferencesManager.getCurrentUserId() } returns "user123"
        
        val activities = listOf(
            CatalogActivityRow(id = "1", name = "Eiffel Tower", category = "attraction", description = "Famous tower")
        )
        vm._activities.value = activities
        vm._destinationLabel.value = "Paris, Francia"

        coEvery { voyagerAiRepository.postLocalRecommendations(any()) } throws RuntimeException("Network error")

        vm.rankCatalog()
        testScope.advanceUntilIdle()

        assertFalse(vm.rankLoading.value)
        assertEquals("Error de red", vm.rankError.value)
        assertTrue(vm.ranked.value.isEmpty())
    }

    @Test
    fun `rankCatalog with empty response`() = runBlocking {
        every { preferencesManager.getCurrentUserId() } returns "user123"
        
        val activities = listOf(
            CatalogActivityRow(id = "1", name = "Eiffel Tower", category = "attraction", description = "Famous tower")
        )
        vm._activities.value = activities
        vm._destinationLabel.value = "Paris, Francia"

        val mockResponse = Response.success(
            """{"items": []}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAiRepository.postLocalRecommendations(any()) } returns mockResponse

        vm.rankCatalog()
        testScope.advanceUntilIdle()

        assertFalse(vm.rankLoading.value)
        assertNull(vm.rankError.value)
        assertTrue(vm.ranked.value.isEmpty())
    }

    @Test
    fun `multiple loadExplore calls work correctly`() = runBlocking {
        val mockResponse1 = Response.success(
            """{"activities": [{"id": "1", "name": "Eiffel Tower"}]}""".toResponseBody("application/json".toMediaType()),
        )
        val mockResponse2 = Response.success(
            """{"activities": [{"id": "2", "name": "Louvre Museum"}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { catalogRepository.activities(any(), any(), any(), any()) } returnsMany listOf(mockResponse1, mockResponse2)

        vm.loadExplore("Paris", "France", "paris-fr")
        testScope.advanceUntilIdle()
        assertEquals("Eiffel Tower", vm.activities.value[0].name)

        vm.loadExplore("Paris", "France", "paris-fr")
        testScope.advanceUntilIdle()
        assertEquals("Louvre Museum", vm.activities.value[0].name)
    }

    @Test
    fun `multiple rankCatalog calls work correctly`() = runBlocking {
        every { preferencesManager.getCurrentUserId() } returns "user123"
        
        val activities = listOf(
            CatalogActivityRow(id = "1", name = "Eiffel Tower", category = "attraction", description = "Famous tower")
        )
        vm._activities.value = activities
        vm._destinationLabel.value = "Paris, Francia"

        val mockResponse1 = Response.success(
            """{"items": [{"name": "Eiffel Tower", "type": "attraction"}]}""".toResponseBody("application/json".toMediaType()),
        )
        val mockResponse2 = Response.success(
            """{"items": [{"name": "Louvre Museum", "type": "museum"}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAiRepository.postLocalRecommendations(any()) } returnsMany listOf(mockResponse1, mockResponse2)

        vm.rankCatalog()
        testScope.advanceUntilIdle()
        assertEquals("Eiffel Tower", vm.ranked.value[0].name)

        vm.rankCatalog()
        testScope.advanceUntilIdle()
        assertEquals("Louvre Museum", vm.ranked.value[0].name)
    }

    @Test
    fun `loading states managed correctly`() = runBlocking {
        val mockResponse = Response.success(
            """{"activities": []}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { catalogRepository.activities(any(), any(), any(), any()) } returns mockResponse

        vm.loadExplore("Paris", "France", "paris-fr")
        assertTrue(vm.catalogLoading.value)
        
        testScope.advanceUntilIdle()
        assertFalse(vm.catalogLoading.value)
    }

    @Test
    fun `ranking loading states managed correctly`() = runBlocking {
        every { preferencesManager.getCurrentUserId() } returns "user123"
        
        val activities = listOf(
            CatalogActivityRow(id = "1", name = "Eiffel Tower", category = "attraction", description = "Famous tower")
        )
        vm._activities.value = activities
        vm._destinationLabel.value = "Paris, Francia"

        val mockResponse = Response.success(
            """{"items": []}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAiRepository.postLocalRecommendations(any()) } returns mockResponse

        vm.rankCatalog()
        assertTrue(vm.rankLoading.value)
        
        testScope.advanceUntilIdle()
        assertFalse(vm.rankLoading.value)
    }
}
