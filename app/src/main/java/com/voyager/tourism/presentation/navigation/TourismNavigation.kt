package com.voyager.tourism.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voyager.tourism.presentation.ui.assistant.AiAssistantScreen
import com.voyager.tourism.presentation.ui.auth.LoginScreen
import com.voyager.tourism.presentation.ui.auth.RegisterScreen
import com.voyager.tourism.presentation.ui.dashboard.DashboardScreen
import com.voyager.tourism.presentation.ui.place.PlaceDetailScreen
import com.voyager.tourism.presentation.ui.preferences.TravelPreferencesScreen
import com.voyager.tourism.presentation.ui.profile.ProfileScreen
import com.voyager.tourism.presentation.ui.recommendations.RecommendationsScreen
import com.voyager.tourism.presentation.ui.social.SocialCollaborationScreen
import com.voyager.tourism.presentation.ui.trip.CreateTravelPlanScreen
import com.voyager.tourism.presentation.ui.trip.TripDetailScreen
import com.voyager.tourism.presentation.ui.trip.TripListScreen
import com.voyager.tourism.presentation.viewmodel.AuthState
import com.voyager.tourism.presentation.viewmodel.AuthViewModel

private const val ROUTE_BOOTSTRAP = "bootstrap"

/**
 * Root [NavHost] wiring authentication gates, dashboard shell, and feature destinations.
 */
@Composable
fun TourismNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.authState.collectAsState()
    val oauthError by authViewModel.oauthError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(oauthError) {
        val msg = oauthError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        authViewModel.clearOAuthError()
    }

    LaunchedEffect(Unit) {
        authViewModel.navigateAfterAuth.collect { route ->
            navController.navigate(route) {
                launchSingleTop = true
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            NavHost(
                navController = navController,
                startDestination = ROUTE_BOOTSTRAP,
            ) {
                composable(ROUTE_BOOTSTRAP) {
                    BootstrapRoute(authState = authState, navController = navController)
                }
                composable(AuthViewModel.ROUTE_LOGIN) {
                    LoginScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                    )
                }
                composable("register") {
                    RegisterScreen(navController = navController)
                }
                composable(AuthViewModel.ROUTE_DASHBOARD) {
                    DashboardScreen(
                        authViewModel = authViewModel,
                        onTripClick = { tripId ->
                            navController.navigate("trip_detail/$tripId") {
                                launchSingleTop = true
                            }
                        },
                        onRecommendationsClick = {
                            navController.navigate("recommendations") {
                                launchSingleTop = true
                            }
                        },
                        onSharedActivitiesClick = {
                            navController.navigate("social") {
                                launchSingleTop = true
                            }
                        },
                        onProfileClick = {
                            navController.navigate("profile") {
                                launchSingleTop = true
                            }
                        },
                        onPlanTripClick = {
                            navController.navigate("create_travel_plan") {
                                launchSingleTop = true
                            }
                        },
                        onAiAssistantClick = {
                            navController.navigate("ai_assistant") {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable("trips") {
                    TripListScreen(
                        authViewModel = authViewModel,
                        onTripClick = { tripId ->
                            navController.navigate("trip_detail/$tripId") {
                                launchSingleTop = true
                            }
                        },
                        onAddTrip = {
                            navController.navigate("create_travel_plan") {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(
                    route = "trip_detail/{tripId}",
                    arguments = listOf(
                        navArgument("tripId") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val tripId = entry.arguments?.getString("tripId").orEmpty()
                    TripDetailScreen(
                        tripId = tripId,
                        onBack = { navController.navigateUp() },
                    )
                }
                composable("create_travel_plan") {
                    CreateTravelPlanScreen(navController = navController)
                }
                composable("recommendations") {
                    RecommendationsScreen(
                        onPlaceClick = { placeId ->
                            navController.navigate("place_detail/$placeId") {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(
                    route = "place_detail/{placeId}",
                    arguments = listOf(
                        navArgument("placeId") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val placeId = entry.arguments?.getString("placeId").orEmpty()
                    PlaceDetailScreen(
                        placeId = placeId,
                        onBack = { navController.navigateUp() },
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(AuthViewModel.ROUTE_LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onBack = { navController.navigateUp() },
                        onTravelPreferences = {
                            navController.navigate("travel_preferences") {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable("travel_preferences") {
                    val currentUser by authViewModel.currentUser.collectAsState()
                    val user = currentUser
                    if (user != null) {
                        TravelPreferencesScreen(
                            userId = user.id,
                            onBack = { navController.popBackStack() },
                        )
                    } else {
                        Text(
                            "Inicia sesión para configurar preferencias.",
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                composable("social") {
                    SocialCollaborationScreen(
                        onBack = { navController.navigateUp() },
                    )
                }
                composable("ai_assistant") {
                    AiAssistantScreen(onBack = { navController.navigateUp() })
                }
            }
        }
    }
}

@Composable
private fun BootstrapRoute(
    authState: AuthState,
    navController: NavHostController,
) {
    LaunchedEffect(authState) {
        if (authState is AuthState.Loading) return@LaunchedEffect
        val target = if (authState is AuthState.Authenticated) {
            AuthViewModel.ROUTE_DASHBOARD
        } else {
            AuthViewModel.ROUTE_LOGIN
        }
        navController.navigate(target) {
            popUpTo(ROUTE_BOOTSTRAP) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
