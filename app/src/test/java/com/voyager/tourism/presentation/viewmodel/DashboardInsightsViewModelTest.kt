package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
    fun `refreshInsights loads trending when IA responds OK`() = runTest {
        val json = """{"emerging_destinations":[{"name":"Paris","country":"FR"}]}"""
        coEvery { voyagerAi.getTrendsDashboard() } returns Response.success(
            json.toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getWeeklyDigest() } returns Response.success(
            """{}""".toResponseBody("application/json".toMediaType()),
        )
        coEvery { voyagerAi.getSeasonalityOverview() } returns Response.success(
            """{}""".toResponseBody("application/json".toMediaType()),
        )

        vm.refreshInsights()
        advanceUntilIdle()

        assertEquals("Paris", vm.trending.value.first().name)
        assertEquals("FR", vm.trending.value.first().country)
    }
}
