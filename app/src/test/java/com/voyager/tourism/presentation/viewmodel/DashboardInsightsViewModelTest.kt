package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertFalse(vm.trendingLoading.value)
    }

    @Test
    fun `refreshInsights loads trending when IA responds OK`() = runBlocking {
        val json = """{"emerging_destinations":[{"name":"Paris","country":"FR"}]}"""
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            json.toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.success(
            """{}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(any()) } returns Response.success(
            """{}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            """{}""".toResponseBody("application/json".toMediaType()),
        )

        vm.refreshInsights()

        // loadTrending usa Dispatchers.IO; en CI el hilo puede tardar más que un withTimeout corto.
        val deadline = System.currentTimeMillis() + 60_000
        while (vm.trending.value.isEmpty() && System.currentTimeMillis() < deadline) {
            delay(25)
        }

        assertTrue(
            "Sin tendencias tras esperar; trendingError=${vm.trendingError.value}",
            vm.trending.value.isNotEmpty(),
        )
        assertEquals("Paris", vm.trending.value.first().name)
        assertEquals("FR", vm.trending.value.first().country)
    }

    @Test
    fun `refreshInsights sets trendingError when trends http fails`() = runBlocking {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.error(
            503,
            "".toResponseBody(null),
        )
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )

        vm.refreshInsights()

        val deadline = System.currentTimeMillis() + 60_000
        while (vm.trendingError.value == null && System.currentTimeMillis() < deadline) {
            delay(25)
        }

        assertNotNull(vm.trendingError.value)
        assertTrue(vm.trending.value.isEmpty())
    }

    @Test
    fun `refreshInsights weekly digest failure sets weeklyError`() = runBlocking {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            """{"emerging_destinations":[]}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.error(
            500,
            "e".toResponseBody("text/plain".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )

        vm.refreshInsights()

        val deadline = System.currentTimeMillis() + 60_000
        while (vm.weeklyError.value == null && System.currentTimeMillis() < deadline) {
            delay(25)
        }

        assertEquals("Digest semanal no disponible.", vm.weeklyError.value)
    }

    @Test
    fun `refreshInsights seasonality failure sets seasonalityError`() = runBlocking {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.error(
            500,
            "".toResponseBody(null),
        )

        vm.refreshInsights()

        val deadline = System.currentTimeMillis() + 60_000
        while (vm.seasonalityError.value == null && System.currentTimeMillis() < deadline) {
            delay(25)
        }

        assertEquals("Panorama estacional no disponible.", vm.seasonalityError.value)
    }

    @Test
    fun `refreshInsights loads weekly and seasonality rows on success`() = runBlocking {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.success(
            """{"micro_trends":[{"title":"A","summary":"B"}]}"""
                .toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            """{"destinations":[{"name":"Calafate"}]}"""
                .toResponseBody("application/json".toMediaType()),
        )

        vm.refreshInsights()

        val deadline = System.currentTimeMillis() + 60_000
        while (
            (vm.weeklyRows.value.isEmpty() || vm.seasonalityRows.value.isEmpty()) &&
            System.currentTimeMillis() < deadline
        ) {
            delay(25)
        }

        assertEquals("A", vm.weeklyRows.value.first().title)
        assertEquals("Calafate", vm.seasonalityRows.value.first().title)
    }
}
