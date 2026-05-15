package com.voyager.tourism.presentation.viewmodel

import com.voyager.tourism.domain.model.PreferenceProfile
import com.voyager.tourism.domain.model.QuestionnaireAnswer
import com.voyager.tourism.domain.model.QuestionnaireQuestion
import com.voyager.tourism.domain.model.QuestionnaireStepResult
import com.voyager.tourism.domain.model.QuestionnaireSubmitResult
import com.voyager.tourism.domain.repository.TravelPreferencesRepository
import com.voyager.tourism.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class TravelPreferencesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<TravelPreferencesRepository>()
    private lateinit var vm: TravelPreferencesViewModel

    @Before
    fun setup() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns mainDispatcherRule.dispatcher
        vm = TravelPreferencesViewModel(repository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Dispatchers::class)
    }

    @Test
    fun `startOrRefresh success`() = runTest {
        val step = QuestionnaireStepResult(
            sessionId = "s1",
            stepIndex = 0,
            isComplete = false,
            derivedPrimaryCategory = "culture",
            questions = listOf(QuestionnaireQuestion("q1", "Hi?", "single", emptyList())),
            message = null,
        )
        coEvery { repository.postQuestionnaireStep("42", null, emptyList()) } returns Result.success(step)

        vm.startOrRefresh("42")
        advanceUntilIdle()

        assertEquals("s1", vm.sessionId.value)
        assertEquals(0, vm.stepIndex.value)
        assertEquals("culture", vm.derivedCategory.value)
        assertEquals(1, vm.questions.value.size)
        assertEquals(false, vm.awaitingFinalSubmit.value)
    }

    @Test
    fun `startOrRefresh failure`() = runTest {
        coEvery { repository.postQuestionnaireStep(any(), any(), any()) } returns Result.failure(RuntimeException("net"))
        vm.startOrRefresh("1")
        advanceUntilIdle()
        assertEquals("net", vm.error.value)
    }

    @Test
    fun `sendStep no session returns early`() = runTest {
        vm.sendStep("1", listOf(QuestionnaireAnswer("q1", listOf("a"))))
        advanceUntilIdle()
        assertNull(vm.sessionId.value)
    }

    @Test
    fun `sendStep success`() = runTest {
        coEvery { repository.postQuestionnaireStep("u", null, emptyList()) } returns Result.success(
            QuestionnaireStepResult("sid", 0, false, null, emptyList(), null),
        )
        vm.startOrRefresh("u")
        advanceUntilIdle()
        val next = QuestionnaireStepResult("sid", 1, true, "adv", emptyList(), "done")
        coEvery {
            repository.postQuestionnaireStep("u", "sid", listOf(QuestionnaireAnswer("q1", listOf("o1"))))
        } returns Result.success(next)

        vm.sendStep("u", listOf(QuestionnaireAnswer("q1", listOf("o1"))))
        advanceUntilIdle()

        assertEquals(1, vm.stepIndex.value)
        assertTrue(vm.awaitingFinalSubmit.value!!)
    }

    @Test
    fun `finalize success`() = runTest {
        coEvery { repository.postQuestionnaireStep(any(), any(), any()) } returns Result.success(
            QuestionnaireStepResult("sid", 0, true, null, emptyList(), null),
        )
        vm.startOrRefresh("u")
        advanceUntilIdle()
        val submit = QuestionnaireSubmitResult(
            userId = "u",
            sessionId = "sid",
            primaryCategory = "x",
            preferenceProfile = PreferenceProfile(emptyList(), null, emptyList(), null, null),
            aiContextSummary = "sum",
        )
        coEvery { repository.submitQuestionnaire("u", "sid", emptyList()) } returns Result.success(submit)

        vm.finalize("u")
        advanceUntilIdle()

        assertEquals("sum", vm.submitResult.value?.aiContextSummary)
    }

    @Test
    fun `finalize without session does nothing observable`() = runTest {
        vm.finalize("u")
        advanceUntilIdle()
        assertNull(vm.submitResult.value)
    }

    @Test
    fun `clearError`() = runTest {
        coEvery { repository.postQuestionnaireStep(any(), any(), any()) } returns Result.failure(RuntimeException("e"))
        vm.startOrRefresh("u")
        advanceUntilIdle()
        vm.clearError()
        assertNull(vm.error.value)
    }
}
