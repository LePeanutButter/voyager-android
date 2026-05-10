package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class PlaceDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val voyagerAi = mockk<VoyagerAiRepository>()
    private val prefs = mockk<PreferencesManager>()
    private lateinit var vm: PlaceDetailViewModel

    @Before
    fun setup() {
        coEvery { prefs.getCurrentUserId() } returns "42"
        vm = PlaceDetailViewModel(voyagerAi, prefs)
    }

    @Test
    fun `loadPlace success clears loading`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            """{"items":[{"id":"1","name":"Tour","category":"x","rating":4.5,"price_label":"","description":""}]}"""
                .toResponseBody("application/json".toMediaType()),
        )

        vm.loadPlace("lima_peru")
        advanceUntilIdle()

        assertFalse(vm.isLoading.value)
        assertTrue(vm.rankedItems.value.isNotEmpty())
    }
}
