package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class PlaceDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<VoyagerAiRepository>()
    private val prefs = mockk<PreferencesManager>()
    private lateinit var vm: PlaceDetailViewModel

    @Before
    fun setup() {
        every { prefs.getCurrentUserId() } returns "user1"
        vm = PlaceDetailViewModel(repo, prefs)
    }

    @Test
    fun `success stores payload`() {
        val body = """{"items":[]}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)
        vm.loadPlace("Paris")
        assertTrue(vm.payload.value?.contains("items") == true)
    }

    @Test
    fun `error response sets error`() {
        coEvery { repo.postLocalRecommendations(any()) } returns Response.error(
            400,
            "bad".toResponseBody(null),
        )
        vm.loadPlace("X")
        assertNotNull(vm.error.value)
    }
}
