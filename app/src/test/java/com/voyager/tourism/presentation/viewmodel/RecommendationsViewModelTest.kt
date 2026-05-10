package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
class RecommendationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>(relaxed = true)
    private val travelRepo = mockk<TravelRepository>(relaxed = true)
    private lateinit var vm: RecommendationsViewModel

    @Before
    fun setup() {
        every { prefs.getCurrentUserId() } returns null
        vm = RecommendationsViewModel(voyagerAi, prefs, travelRepo)
    }

    private suspend fun awaitNotLoading() {
        val deadline = System.currentTimeMillis() + 60_000
        while (vm.isLoading.value && System.currentTimeMillis() < deadline) {
            delay(10)
        }
    }

    @Test
    fun `load anonymous user fetches default candidates and maps rows`() = runBlocking {
        every { prefs.getCurrentUserId() } returns null
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            """{"items":[{"id":"1","name":"R1","category":"c","score":0.5,"description":"d"}]}"""
                .toResponseBody("application/json".toMediaType()),
        )

        vm.load()
        awaitNotLoading()

        coVerify(exactly = 0) { travelRepo.getUserTravelPlans(any()) }
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
        assertEquals(1, vm.rows.value.size)
        assertEquals("R1", vm.rows.value.first().name)
    }

    @Test
    fun `load uses travel plans when user is logged in`() = runBlocking {
        every { prefs.getCurrentUserId() } returns "42"
        val plan = TestFixtures.travelPlanDto(destination = "Tokyo")
        coEvery { travelRepo.getUserTravelPlans("42") } returns Result.success(listOf(plan))
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            """{"items":[{"id":"x","name":"Y","category":"z","score":0.2,"description":"d"}]}"""
                .toResponseBody("application/json".toMediaType()),
        )

        vm.load()
        awaitNotLoading()

        coVerify(atLeast = 1) { travelRepo.getUserTravelPlans("42") }
        assertEquals("Y", vm.rows.value.first().name)
        assertNotNull(vm.rows.value.first().category)
    }

    @Test
    fun `load sets error on unsuccessful http response`() = runBlocking {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.error(
            503,
            "unavailable".toResponseBody("text/plain".toMediaType()),
        )

        vm.load()
        awaitNotLoading()

        assertTrue(vm.rows.value.isEmpty())
        assertNotNull(vm.error.value)
    }

    @Test
    fun `load sets error when service returns no items`() = runBlocking {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            "{}".toResponseBody("application/json".toMediaType()),
        )

        vm.load()
        awaitNotLoading()

        assertTrue(vm.rows.value.isEmpty())
        assertNotNull(vm.error.value)
    }

    @Test
    fun `load sets error on network exception`() = runBlocking {
        coEvery { voyagerAi.postLocalRecommendations(any()) } throws IOException("net down")

        vm.load()
        awaitNotLoading()

        assertTrue(vm.rows.value.isEmpty())
        assertNotNull(vm.error.value)
    }

    @Test
    fun `submitFeedback without user shows message`() {
        every { prefs.getCurrentUserId() } returns null
        vm.submitFeedback("it1", 4)
        assertTrue(vm.feedbackMessage.value?.contains("sesión") == true)
    }

    @Test
    fun `submitFeedback success shows confirmation`() = runBlocking {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery {
            voyagerAi.postLocalRecommendationFeedback(userId = "42", itemId = "it1", rating = 4)
        } returns Response.success("ok".toResponseBody("text/plain".toMediaType()))

        vm.submitFeedback("it1", 4)
        val deadline = System.currentTimeMillis() + 10_000
        while (vm.feedbackMessage.value == null && System.currentTimeMillis() < deadline) {
            delay(10)
        }

        assertEquals("Valoración enviada", vm.feedbackMessage.value)
    }

    @Test
    fun `submitFeedback failure maps error body`() = runBlocking {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery {
            voyagerAi.postLocalRecommendationFeedback(any(), any(), any())
        } returns Response.error(
            400,
            "bad".toResponseBody("text/plain".toMediaType()),
        )

        vm.submitFeedback("it1", 2)
        val deadline = System.currentTimeMillis() + 10_000
        while (vm.feedbackMessage.value == null && System.currentTimeMillis() < deadline) {
            delay(10)
        }

        assertEquals("bad", vm.feedbackMessage.value)
    }

    @Test
    fun `submitFeedback exception shows message`() = runBlocking {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { voyagerAi.postLocalRecommendationFeedback(any(), any(), any()) } throws IOException("x")

        vm.submitFeedback("it1", 5)
        val deadline = System.currentTimeMillis() + 10_000
        while (vm.feedbackMessage.value == null && System.currentTimeMillis() < deadline) {
            delay(10)
        }

        assertEquals("x", vm.feedbackMessage.value)
    }

    @Test
    fun `submitFeedback clamps rating`() = runBlocking {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery {
            voyagerAi.postLocalRecommendationFeedback("42", "it1", 5)
        } returns Response.success("".toResponseBody(null))

        vm.submitFeedback("it1", 99)
        val deadline = System.currentTimeMillis() + 10_000
        while (vm.feedbackMessage.value == null && System.currentTimeMillis() < deadline) {
            delay(10)
        }

        coVerify { voyagerAi.postLocalRecommendationFeedback("42", "it1", 5) }
    }

    @Test
    fun `clearFeedbackMessage clears state`() {
        every { prefs.getCurrentUserId() } returns null
        vm.submitFeedback("x", 3)
        vm.clearFeedbackMessage()
        assertNull(vm.feedbackMessage.value)
    }
}
