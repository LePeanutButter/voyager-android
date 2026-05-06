package com.voyager.tourism.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voyager.tourism.presentation.ui.auth.LoginScreen
import com.voyager.tourism.presentation.ui.auth.RegisterScreen
import com.voyager.tourism.presentation.ui.dashboard.DashboardScreen
import com.voyager.tourism.presentation.ui.trip.TripListScreen
import com.voyager.tourism.presentation.ui.trip.CreateTravelPlanScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.presentation.ui.profile.ProfileScreen
import com.voyager.tourism.presentation.ui.preferences.TravelPreferencesScreen
import com.voyager.tourism.presentation.ui.recommendations.RecommendationsScreen
import com.voyager.tourism.presentation.viewmodel.AuthViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Main navigation component for the Tourism Intelligent Platform
 * Handles navigation between different screens using Jetpack Navigation Compose
 */
@Composable
fun TourismNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Authentication screens
        composable("login") {
            LoginScreen(navController = navController)
        }
        
        composable("register") {
            RegisterScreen(navController = navController)
        }
        
        // Main app screens
        composable("dashboard") {
            DashboardScreen(
                onTripClick = { tripId ->
                    navController.navigate("trip_detail/$tripId")
                },
                onRecommendationsClick = {
                    navController.navigate("recommendations")
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }
        
        composable("trips") {
            TripListScreen(
                onTripClick = { tripId ->
                    navController.navigate("trip_detail/$tripId")
                },
                onAddTrip = {
                    navController.navigate("create_trip")
                }
            )
        }
        
        composable("trip_detail/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            // Trip detail screen implementation
        }
        
        composable("create_travel_plan") {
            CreateTravelPlanScreen(navController = navController)
        }
        
        composable("recommendations") {
            RecommendationsScreen(
                onPlaceClick = { placeId ->
                    navController.navigate("place_detail/$placeId")
                }
            )
        }
        
        composable("place_detail/{placeId}") { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
            // Place detail screen implementation
        }
        
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() },
                onTravelPreferences = { navController.navigate("travel_preferences") }
            )
        }

        composable("travel_preferences") {
            val authViewModel: AuthViewModel = hiltViewModel()
            val currentUser by authViewModel.currentUser.collectAsState()
            val user = currentUser
            if (user != null) {
                TravelPreferencesScreen(
                    userId = user.id,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text(
                    "Inicia sesión para configurar preferencias.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        composable("social") {
            // Social features screen implementation
        }
        
        composable("ai_assistant") {
            // AI assistant chat screen implementation
        }
    }
}
