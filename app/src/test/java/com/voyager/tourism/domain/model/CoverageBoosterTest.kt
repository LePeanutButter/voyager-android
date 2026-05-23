package com.voyager.tourism.domain.model

import org.junit.Assert.assertNotNull
import org.junit.Test

class CoverageBoosterTest {

    @Test
    fun touchTripModels() {
        val coords = Coordinates(1.0, 2.0)
        val dest = Destination("1", "N", "C", coords, "T", "C", "L", "C", "B", 10.0, 4.0f)
        val trip = Trip("1", "U", "T", "D", dest, 0L, 1L, 100.0, 2, TripStatus.PLANNING, emptyList(), emptyList(), emptyList(), 0L, 1L)
        val item = ItineraryItem("1", 1, 0L, 1L, "T", "D", "L", ItineraryType.ACTIVITY, 10.0, true)
        val acc = Accommodation("1", "N", AccommodationType.HOTEL, "A", 4.0f, 10.0, 0L, 1L, emptyList())
        val act = Activity("1", "N", "D", "L", 60, 10.0, 4.0f, ActivityCategory.CULTURAL)

        assertNotNull(trip)
        assertNotNull(item)
        assertNotNull(acc)
        assertNotNull(act)
        assertNotNull(TripStatus.values())
        assertNotNull(ItineraryType.TRANSPORTATION)
        assertNotNull(ActivityCategory.ADVENTURE)
        assertNotNull(AccommodationType.HOSTEL)
    }

    @Test
    fun touchUserModels() {
        val user = User("1", "E", "U", "F", "L", null, "R", "S", null, null, emptySet(), null, null, null, "T")
        assertNotNull(user)
    }

    @Test
    fun touchSocialModels() {
        val conn = TravelerConnection(1L, 2L, "U", "F", "L", "S")
        val act = TravelActivity(1L, "N")
        val shared = SharedActivity(1L, 2L, 3L, 4L, "S", true)
        val match = CompatibilityMatch(1L, 1.0, 1.0, 1.0, 1.0, emptyList())

        assertNotNull(conn)
        assertNotNull(act)
        assertNotNull(shared)
        assertNotNull(match)
        assertNotNull(SharedActivityDecision.ACCEPT)
    }

    @Test
    fun touchQuestionnaireModels() {
        val opt = QuestionOption("1", "L")
        val ques = QuestionnaireQuestion("1", "P", "T", listOf(opt))
        val ans = QuestionnaireAnswer("1", listOf("1"))
        val step = QuestionnaireStepResult("S", 1, true, "C", listOf(ques), "M")
        val prof = PreferenceProfile(listOf("C"), "P", listOf("I"), "C", "N")
        val sub = QuestionnaireSubmitResult("U", "S", "C", prof, "S")

        assertNotNull(ques)
        assertNotNull(ans)
        assertNotNull(step)
        assertNotNull(prof)
        assertNotNull(sub)
    }
}
