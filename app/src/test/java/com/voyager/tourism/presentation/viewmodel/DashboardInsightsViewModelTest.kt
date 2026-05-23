package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.AiSeasonalityOverviewDto
import com.voyager.tourism.data.dto.AiTrendDashboardDto
import com.voyager.tourism.data.dto.TrendItemDto
import com.voyager.tourism.data.dto.WeeklyDigestDto
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestDispatcherProvider
import io.mockk.coEvery
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
import org.junit.Assert.assertFalse
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
class DashboardInsightsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val voyagerAi = mockk<VoyagerAiRepository>()
    private val dispatchers = TestDispatcherProvider(testDispatcher)
    private lateinit var vm: DashboardInsightsViewModel

    @Before
    fun setup() {
        vm = DashboardInsightsViewModel(voyagerAi, dispatchers)
    }

    @After
    fun tearDown() {
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
    fun `refreshInsights loads trending when IA responds OK`() = runTest {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            AiTrendDashboardDto(
                listOf(TrendItemDto("paris-123", "Paris", 0.5, 90.0)),
                emptyList(),
                emptyList()
            )
        )
        coEvery { voyagerAi.getWeeklyDigestTyped() } returns Response.success(
            WeeklyDigestDto(raw = emptyMap())
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            AiSeasonalityOverviewDto(1, emptyMap(), emptyList(), emptyList())
        )

        vm.refreshInsights()
        advanceUntilIdle()

        assertTrue(
            "Sin tendencias tras esperar; trendingError=${vm.trendingError.value}",
            vm.trending.value.isNotEmpty(),
        )
        assertEquals("Paris", vm.trending.value.first().name)
    }

    @Test
    fun `refreshInsights sets trendingError when trends http fails`() = runTest {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.error(
            503,
            "".toResponseBody(null),
        )
        coEvery { voyagerAi.getWeeklyDigestTyped() } returns Response.success(
            WeeklyDigestDto(raw = emptyMap())
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            AiSeasonalityOverviewDto(1, emptyMap(), emptyList(), emptyList())
        )

        vm.refreshInsights()
        advanceUntilIdle()

        assertNotNull(vm.trendingError.value)
        assertTrue(vm.trending.value.isEmpty())
    }

    @Test
    fun `refreshInsights weekly digest failure sets weeklyError`() = runTest {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            AiTrendDashboardDto(emptyList(), emptyList(), emptyList())
        )
        coEvery { voyagerAi.getWeeklyDigestTyped() } returns Response.error(
            500,
            "e".toResponseBody("text/plain".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            AiSeasonalityOverviewDto(1, emptyMap(), emptyList(), emptyList())
        )

        vm.refreshInsights()
        advanceUntilIdle()

        assertEquals("Digest semanal no disponible.", vm.weeklyError.value)
    }

    @Test
    fun `refreshInsights seasonality failure sets seasonalityError`() = runTest {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            AiTrendDashboardDto(emptyList(), emptyList(), emptyList())
        )
        coEvery { voyagerAi.getWeeklyDigestTyped() } returns Response.success(
            WeeklyDigestDto(raw = emptyMap())
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.error(
            500,
            "".toResponseBody(null),
        )

        vm.refreshInsights()
        advanceUntilIdle()

        assertEquals("Panorama estacional no disponible.", vm.seasonalityError.value)
    }

    @Test
    fun `refreshInsights loads weekly and seasonality rows on success`() = runTest {
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            AiTrendDashboardDto(emptyList(), emptyList(), emptyList())
        )
        coEvery { voyagerAi.getWeeklyDigestTyped() } returns Response.success(
            WeeklyDigestDto(
                raw = mapOf(
                    "items" to listOf(
                        mapOf("id" to "1", "title" to "A")
                    )
                )
            )
        )
        coEvery { voyagerAi.getSeasonalityOverview(null) } returns Response.success(
            AiSeasonalityOverviewDto(1, emptyMap(), listOf("Calafate"), emptyList())
        )

        vm.refreshInsights()
        advanceUntilIdle()

        assertTrue(vm.weeklyRows.value.isNotEmpty())
        assertEquals("A", vm.weeklyRows.value.first().title)
        assertEquals("Calafate", vm.seasonalityRows.value.first().title)
    }
}

