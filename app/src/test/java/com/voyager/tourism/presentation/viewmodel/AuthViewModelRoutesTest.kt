package com.voyager.tourism.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** [android.net.Uri] no está implementado en stubs JVM; Robolectric proporciona sombras. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AuthViewModelRoutesTest {

    @Test
    fun `companion route helpers build navigable paths`() {
        assertEquals("trip_detail/t1", AuthViewModel.createTripDetailRoute("t1"))
        assertEquals("place_detail/p99", AuthViewModel.createPlaceDetailRoute("p99"))
        val planRoute = AuthViewModel.createTravelPlanRoute("Paris")
        assertTrue(planRoute.startsWith("create_travel_plan?hint="))
        val explore = AuthViewModel.createDestinationExploreRoute("Lima", "PE", "dest-1")
        assertTrue(explore.startsWith("destination_explore?"))
        assertTrue(explore.contains("loc=") && explore.contains("country=") && explore.contains("destId="))
        val exploreFallback = AuthViewModel.createDestinationExploreRoute("   ", null, "id-only")
        assertTrue(exploreFallback.contains("destId="))
    }
}
