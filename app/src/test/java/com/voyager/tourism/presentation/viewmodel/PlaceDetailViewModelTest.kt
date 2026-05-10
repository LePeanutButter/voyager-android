package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.data.local.PreferencesManager
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

class PlaceDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<VoyagerAiRepository>()
    private val prefs = mockk<PreferencesManager>()
    private lateinit var vm: PlaceDetailViewModel
    private val testScope = TestScope()

    @Before
    fun setup() {
        every { prefs.getCurrentUserId() } returns "user1"
        vm = PlaceDetailViewModel(repo, prefs)
    }

    @Test
    fun `initial state is loading`() {
        assertTrue(vm.isLoading.value)
        assertNull(vm.error.value)
        assertNull(vm.payload.value)
        assertTrue(vm.rankedItems.value.isEmpty())
    }

    @Test
    fun `success stores payload`() = runBlocking {
        val body = """{"items": [{"name": "Paris", "type": "city"}, {"name": "Beach", "type": "activity"}]}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)
        vm.loadPlace("Paris")
        testScope.advanceUntilIdle()

        assertEquals(2, vm.rankedItems.value.size)
        assertEquals("Paris", vm.rankedItems.value[0].name)
        assertEquals("city", vm.rankedItems.value[0].type)
        assertEquals("Beach", vm.rankedItems.value[1].name)
        assertEquals("activity", vm.rankedItems.value[1].type)
        assertTrue(vm.payload.value?.contains("items") == true)
        assertNull(vm.error.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `error response sets error`() = runBlocking {
        coEvery { repo.postLocalRecommendations(any()) } returns Response.error(
            400,
            "bad".toResponseBody(null),
        )
        vm.loadPlace("X")
        testScope.advanceUntilIdle()

        assertNotNull(vm.error.value)
        assertTrue(vm.rankedItems.value.isEmpty())
        assertNull(vm.payload.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `network exception sets error`() = runBlocking {
        coEvery { repo.postLocalRecommendations(any()) } throws RuntimeException("Network error")
        vm.loadPlace("Paris")
        testScope.advanceUntilIdle()

        assertEquals("Network error", vm.error.value)
        assertTrue(vm.rankedItems.value.isEmpty())
        assertNull(vm.payload.value)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `empty response clears items`() = runBlocking {
        val body = """{"items": []}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)
        vm.loadPlace("Paris")
        testScope.advanceUntilIdle()

        assertTrue(vm.rankedItems.value.isEmpty())
        assertNull(vm.error.value)
        assertTrue(vm.payload.value?.contains("items") == true)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `malformed JSON response handles gracefully`() = runBlocking {
        val body = """{"invalid": "json"}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)
        vm.loadPlace("Paris")
        testScope.advanceUntilIdle()

        assertTrue(vm.rankedItems.value.isEmpty())
        assertNull(vm.error.value)
        assertTrue(vm.payload.value?.contains("invalid") == true)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `null response body handles gracefully`() = runBlocking {
        val body = """{"items": null}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)
        vm.loadPlace("Paris")
        testScope.advanceUntilIdle()

        assertTrue(vm.rankedItems.value.isEmpty())
        assertNull(vm.error.value)
        assertTrue(vm.payload.value?.contains("null") == true)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `multiple load calls work correctly`() = runBlocking {
        val body = """{"items": [{"name": "Paris"}]}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)

        vm.loadPlace("Paris")
        testScope.advanceUntilIdle()
        assertEquals(1, vm.rankedItems.value.size)

        vm.loadPlace("London")
        testScope.advanceUntilIdle()
        assertEquals(1, vm.rankedItems.value.size)
        assertEquals("London", vm.rankedItems.value[0].name)
    }

    @Test
    fun `load with empty place ID`() = runBlocking {
        val body = """{"items": [{"name": "Paris"}]}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)

        vm.loadPlace("")
        testScope.advanceUntilIdle()

        assertTrue(vm.rankedItems.value.isEmpty())
        assertEquals("Place ID cannot be empty", vm.error.value)
    }

    @Test
    fun `load with whitespace place ID`() = runBlocking {
        val body = """{"items": [{"name": "Paris"}]}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)

        vm.loadPlace("   ")
        testScope.advanceUntilIdle()

        assertTrue(vm.rankedItems.value.isEmpty())
        assertEquals("Place ID cannot be empty", vm.error.value)
    }

    @Test
    fun `technical detail toggle works`() = runBlocking {
        val body = """{"items": [{"name": "Paris"}]}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)

        vm.loadPlace("Paris")
        testScope.advanceUntilIdle()

        // Initially false
        assertFalse(vm.showTechnicalDetail.value)

        vm.toggleTechnicalDetail()
        assertTrue(vm.showTechnicalDetail.value)

        vm.toggleTechnicalDetail()
        assertFalse(vm.showTechnicalDetail.value)
    }

    @Test
    fun `clearError clears error state`() = runBlocking {
        vm._error.value = "Test error"
        vm.clearError()

        assertNull(vm.error.value)
    }

    @Test
    fun `error state cleared on successful load`() = runBlocking {
        val body = """{"items": [{"name": "Paris"}]}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)

        vm._error.value = "Previous error"
        vm.loadPlace("Paris")
        testScope.advanceUntilIdle()

        assertNull(vm.error.value)
    }

    @Test
    fun `loading state managed correctly`() = runBlocking {
        val body = """{"items": [{"name": "Paris"}]}""".toResponseBody("application/json".toMediaType())
        coEvery { repo.postLocalRecommendations(any()) } returns Response.success(body)

        vm.loadPlace("Paris")
        assertTrue(vm.isLoading.value)

        testScope.advanceUntilIdle()
        assertFalse(vm.isLoading.value)
    }
}
