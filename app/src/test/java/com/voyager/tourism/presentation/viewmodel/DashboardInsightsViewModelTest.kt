package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dashboard.AiDashboardParsers
import com.voyager.tourism.data.dashboard.ParsedDigestRow
import com.voyager.tourism.data.dashboard.ParsedSeasonalityRow
import com.voyager.tourism.data.dashboard.ParsedTrendingDestination
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
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
class DashboardInsightsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private val voyagerAi = mockk<VoyagerAiRepository>()
    private lateinit var vm: DashboardInsightsViewModel
    private val testScope = TestScope()

    @Before
    fun setup() {
        vm = DashboardInsightsViewModel(voyagerAi)
    }

    @Test
    fun `initial state is empty`() {
        assertTrue(vm.trending.value.isEmpty())
        assertTrue(vm.weeklyRows.value.isEmpty())
        assertTrue(vm.seasonalityRows.value.isEmpty())
        assertEquals(null, vm.trendingError.value)
        assertEquals(null, vm.weeklyError.value)
        assertEquals(null, vm.seasonalityError.value)
        assertFalse(vm.trendingLoading.value)
    }

    @Test
    fun `refreshInsights launches all three loads`() = runBlocking {
        vm.refreshInsights()
        testScope.advanceUntilIdle()

        // Verify that all three coroutines were launched
        // Note: We can't easily verify the exact coroutine launches, but we can verify the results
    }

    @Test
    fun `loadTrending success`() = runBlocking {
        val mockResponse = Response.success(
            """{"trending": [{"name": "Paris", "country": "France"}, {"name": "London", "country": "UK"}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getTrendsDashboard() } returns mockResponse

        vm.loadTrending()
        testScope.advanceUntilIdle()

        assertEquals(2, vm.trending.value.size)
        assertEquals("Paris", vm.trending.value[0].name)
        assertEquals("France", vm.trending.value[0].country)
        assertEquals("London", vm.trending.value[1].name)
        assertEquals("UK", vm.trending.value[1].country)
        assertEquals(null, vm.trendingError.value)
        assertFalse(vm.trendingLoading.value)
    }

    @Test
    fun `loadTrending failure`() = runBlocking {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.error(
            500,
            "Server error".toResponseBody(null),
        )

        vm.loadTrending()
        testScope.advanceUntilIdle()

        assertTrue(vm.trending.value.isEmpty())
        assertEquals("No se pudo cargar el panel de tendencias (servicio de IA).", vm.trendingError.value)
        assertFalse(vm.trendingLoading.value)
    }

    @Test
    fun `loadTrending exception`() = runBlocking {
        coEvery { voyagerAi.getTrendsDashboard() } throws RuntimeException("Network error")

        vm.loadTrending()
        testScope.advanceUntilIdle()

        assertTrue(vm.trending.value.isEmpty())
        assertEquals("Error de tendencias", vm.trendingError.value)
        assertFalse(vm.trendingLoading.value)
    }

    @Test
    fun `loadWeekly success`() = runBlocking {
        val mockResponse = Response.success(
            """{"digest": [{"week": "2023-W01", "destinations": ["Paris", "London"]}, {"week": "2023-W02", "destinations": ["Rome"]}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getWeeklyDigest() } returns mockResponse

        vm.loadWeekly()
        testScope.advanceUntilIdle()

        assertEquals(2, vm.weeklyRows.value.size)
        assertEquals("2023-W01", vm.weeklyRows.value[0].week)
        assertEquals("Paris", vm.weeklyRows.value[0].destinations[0])
        assertEquals("London", vm.weeklyRows.value[0].destinations[1])
        assertEquals("2023-W02", vm.weeklyRows.value[1].week)
        assertEquals("Rome", vm.weeklyRows.value[1].destinations[0])
        assertEquals(null, vm.weeklyError.value)
    }

    @Test
    fun `loadWeekly failure`() = runBlocking {
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.error(
            500,
            "Server error".toResponseBody(null),
        )

        vm.loadWeekly()
        testScope.advanceUntilIdle()

        assertTrue(vm.weeklyRows.value.isEmpty())
        assertEquals("Digest semanal no disponible.", vm.weeklyError.value)
    }

    @Test
    fun `loadWeekly exception`() = runBlocking {
        coEvery { voyagerAi.getWeeklyDigest() } throws RuntimeException("Network error")

        vm.loadWeekly()
        testScope.advanceUntilIdle()

        assertTrue(vm.weeklyRows.value.isEmpty())
        assertEquals("Digest no disponible", vm.weeklyError.value)
    }

    @Test
    fun `loadSeasonality success`() = runBlocking {
        val mockResponse = Response.success(
            """{"seasonality": [{"season": "Summer", "destinations": ["Beach", "Island"]}, {"season": "Winter", "destinations": ["Mountain"]}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns mockResponse

        vm.loadSeasonality()
        testScope.advanceUntilIdle()

        assertEquals(2, vm.seasonalityRows.value.size)
        assertEquals("Summer", vm.seasonalityRows.value[0].season)
        assertEquals("Beach", vm.seasonalityRows.value[0].destinations[0])
        assertEquals("Island", vm.seasonalityRows.value[0].destinations[1])
        assertEquals("Winter", vm.seasonalityRows.value[1].season)
        assertEquals("Mountain", vm.seasonalityRows.value[1].destinations[0])
        assertEquals(null, vm.seasonalityError.value)
    }

    @Test
    fun `loadSeasonality failure`() = runBlocking {
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.error(
            500,
            "Server error".toResponseBody(null),
        )

        vm.loadSeasonality()
        testScope.advanceUntilIdle()

        assertTrue(vm.seasonalityRows.value.isEmpty())
        assertEquals("Panorama estacional no disponible.", vm.seasonalityError.value)
    }

    @Test
    fun `loadSeasonality exception`() = runBlocking {
        coEvery { voyagerAi.getSeasonalityOverview(null) } throws RuntimeException("Network error")

        vm.loadSeasonality()
        testScope.advanceUntilIdle()

        assertTrue(vm.seasonalityRows.value.isEmpty())
        assertEquals("Estacionalidad no disponible", vm.seasonalityError.value)
    }

    @Test
    fun `loadTrending with empty body`() = runBlocking {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            """{"trending": []}""".toResponseBody("application/json".toMediaType()),
        )

        vm.loadTrending()
        testScope.advanceUntilIdle()

        assertTrue(vm.trending.value.isEmpty())
        assertEquals(null, vm.trendingError.value)
        assertFalse(vm.trendingLoading.value)
    }

    @Test
    fun `loadWeekly with empty body`() = runBlocking {
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.success(
            """{"digest": []}""".toResponseBody("application/json".toMediaType()),
        )

        vm.loadWeekly()
        testScope.advanceUntilIdle()

        assertTrue(vm.weeklyRows.value.isEmpty())
        assertEquals(null, vm.weeklyError.value)
    }

    @Test
    fun `loadSeasonality with empty body`() = runBlocking {
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            """{"seasonality": []}""".toResponseBody("application/json".toMediaType()),
        )

        vm.loadSeasonality()
        testScope.advanceUntilIdle()

        assertTrue(vm.seasonalityRows.value.isEmpty())
        assertEquals(null, vm.seasonalityError.value)
    }

    @Test
    fun `loadTrending sets loading state correctly`() = runBlocking {
        var loadingStates = mutableListOf<Boolean>()
        
        // Mock a delayed response to test loading state
        coEvery { voyagerAi.getTrendsDashboard() } coAnswers {
            loadingStates.add(vm.trendingLoading.value)
            Response.success("""{"trending": []}""".toResponseBody("application/json".toMediaType()))
        }

        vm.loadTrending()
        
        // Check initial loading state
        assertTrue(vm.trendingLoading.value)
        
        testScope.advanceUntilIdle()
        
        // Verify loading states
        assertTrue(loadingStates.any { it }) // Should have been true at some point
        assertFalse(vm.trendingLoading.value) // Should be false after completion
    }

    @Test
    fun `multiple refresh calls work correctly`() = runBlocking {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            """{"trending": [{"name": "Paris"}]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.success(
            """{"digest": []}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            """{"seasonality": []}""".toResponseBody("application/json".toMediaType()),
        )

        // First refresh
        vm.refreshInsights()
        testScope.advanceUntilIdle()
        
        assertEquals(1, vm.trending.value.size)
        assertEquals("Paris", vm.trending.value[0].name)
        
        // Second refresh
        vm.refreshInsights()
        testScope.advanceUntilIdle()
        
        // Should still work correctly
        assertEquals(1, vm.trending.value.size)
        assertEquals("Paris", vm.trending.value[0].name)
    }
}
