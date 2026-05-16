package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.dto.AiMatchingResponseDto
import com.voyager.tourism.data.dto.AiTravelerMatchDto
import com.voyager.tourism.data.dto.TravelType
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.repository.TravelRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.MainDispatcherRule
import com.voyager.tourism.util.TestDispatcherProvider
import com.voyager.tourism.util.TestFixtures
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class RecommendationsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val voyagerAi = mockk<VoyagerAiRepository>(relaxed = true)
    private val prefs = mockk<PreferencesManager>(relaxed = true)
    private val travelRepo = mockk<TravelRepository>(relaxed = true)
    private val dispatchers = TestDispatcherProvider(testDispatcher)
    private lateinit var vm: RecommendationsViewModel

    @Before
    fun setup() {
        clearMocks(voyagerAi, prefs, travelRepo)

        every { prefs.getCurrentUserId() } returns null
        vm = RecommendationsViewModel(voyagerAi, prefs, travelRepo, dispatchers)
    }

    @After
    fun tearDown() {
        // No longer needed
    }


    @Test
    fun `load anonymous user fetches default candidates and maps rows`() = runTest {
        every { prefs.getCurrentUserId() } returns null
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            AiMatchingResponseDto(
                matches = listOf(
                    AiTravelerMatchDto("u1", "R1", 25, 80.0, emptyList(), 0.8, "Bio 1")
                ),
                userId = "anonymous",
                totalMatches = 1
            )
        )

        vm.load()
        advanceUntilIdle()

        coVerify(exactly = 0) { travelRepo.getUserTravelPlans(any()) }
        assertFalse(vm.isLoading.value)
        assertNull(vm.error.value)
        assertEquals(1, vm.rows.value.size)
        assertEquals("R1", vm.rows.value.first().name)
    }

    @Test
    fun `load uses travel plans when user is logged in`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        val plan = TestFixtures.travelPlanDto(destination = "Tokyo")
        coEvery { travelRepo.getUserTravelPlans("42") } returns Result.success(listOf(plan))
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            AiMatchingResponseDto(
                matches = listOf(
                    AiTravelerMatchDto("u2", "Y", 30, 95.0, emptyList(), 0.9, "Bio 2")
                ),
                userId = "42",
                totalMatches = 1
            )
        )

        vm.load()
        advanceUntilIdle()

        coVerify(atLeast = 1) { travelRepo.getUserTravelPlans("42") }
        assertEquals("Y", vm.rows.value.first().name)
        assertNotNull(vm.rows.value.first().category)
    }

    @Test
    fun `load sets error on unsuccessful http response`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.error(
            503,
            "unavailable".toResponseBody("text/plain".toMediaType()),
        )

        vm.load()
        advanceUntilIdle()

        assertTrue(vm.rows.value.isEmpty())
        assertNotNull(vm.error.value)
        assertTrue(vm.error.value!!.contains("HTTP 503"))
    }

    @Test
    fun `load sets error when service returns no items`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            AiMatchingResponseDto(emptyList(), "42", 0)
        )

        vm.load()
        advanceUntilIdle()

        assertTrue(vm.rows.value.isEmpty())
        assertNotNull(vm.error.value)
    }

    @Test
    fun `load sets error on network exception`() = runTest {
        coEvery { voyagerAi.postLocalRecommendations(any()) } throws IOException("net down")

        vm.load()
        advanceUntilIdle()

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
    fun `submitFeedback success shows confirmation`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery {
            voyagerAi.postLocalRecommendationFeedback(userId = "42", itemId = "it1", rating = 4)
        } returns Response.success(Unit)

        vm.submitFeedback("it1", 4)
        advanceUntilIdle()

        assertEquals("Valoración enviada", vm.feedbackMessage.value)
    }

    @Test
    fun `submitFeedback failure maps error body`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery {
            voyagerAi.postLocalRecommendationFeedback(any(), any(), any())
        } returns Response.error(
            400,
            "bad".toResponseBody("text/plain".toMediaType()),
        )

        vm.submitFeedback("it1", 2)
        advanceUntilIdle()

        assertEquals("No se pudo registrar la valoración", vm.feedbackMessage.value)
    }

    @Test
    fun `submitFeedback exception shows message`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { voyagerAi.postLocalRecommendationFeedback(any(), any(), any()) } throws IOException("x")

        vm.submitFeedback("it1", 5)
        advanceUntilIdle()

        assertEquals("x", vm.feedbackMessage.value)
    }

    @Test
    fun `submitFeedback clamps rating`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery {
            voyagerAi.postLocalRecommendationFeedback("42", "it1", 5)
        } returns Response.success(Unit)

        vm.submitFeedback("it1", 99)
        advanceUntilIdle()

        coVerify { voyagerAi.postLocalRecommendationFeedback("42", "it1", 5) }
    }

    @Test
    fun `clearFeedbackMessage clears state`() {
        every { prefs.getCurrentUserId() } returns null
        vm.submitFeedback("x", 3)
        vm.clearFeedbackMessage()
        assertNull(vm.feedbackMessage.value)
    }

    @Test
    fun `submitFeedback http error with empty body yields empty message`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        coEvery {
            voyagerAi.postLocalRecommendationFeedback(any(), any(), any())
        } returns Response.error(500, "".toResponseBody(null))

        vm.submitFeedback("it1", 3)
        advanceUntilIdle()

        assertEquals("No se pudo registrar la valoración", vm.feedbackMessage.value)
    }

    @Test
    fun `load maps travelType into candidate category`() = runTest {
        every { prefs.getCurrentUserId() } returns "42"
        val plan = TestFixtures.travelPlanDto().copy(travelType = TravelType.CULTURAL)
        coEvery { travelRepo.getUserTravelPlans("42") } returns Result.success(listOf(plan))
        coEvery { voyagerAi.postLocalRecommendations(any()) } returns Response.success(
            AiMatchingResponseDto(
                matches = listOf(
                    AiTravelerMatchDto("u3", "R", 20, 70.0, emptyList(), 0.7)
                ),
                userId = "42",
                totalMatches = 1
            )
        )

        vm.load()
        advanceUntilIdle()

        assertEquals("R", vm.rows.value.first().name)
    }
}

