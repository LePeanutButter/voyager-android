package com.voyager.tourism.presentation.viewmodel

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

        vm.refreshInsights()
        // loadTrending usa Dispatchers.IO; advanceUntilIdle no espera ese hilo.
        val rows = withTimeout(5_000) {
            vm.trending.first { it.isNotEmpty() }
        }
        assertEquals("Paris", rows.first().name)
        assertEquals("FR", rows.first().country)
    }
}
