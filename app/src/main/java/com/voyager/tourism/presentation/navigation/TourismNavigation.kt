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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voyager.tourism.presentation.ui.assistant.AiAssistantScreen
import com.voyager.tourism.presentation.ui.auth.LoginScreen
import com.voyager.tourism.presentation.ui.auth.RegisterScreen
import com.voyager.tourism.presentation.ui.behavior.BehaviorAnalysisScreen
import com.voyager.tourism.presentation.ui.calendar.ScheduleCalendarScreen
import com.voyager.tourism.presentation.ui.dashboard.DashboardScreen
import com.voyager.tourism.presentation.navigation.DestinationExploreParams
import com.voyager.tourism.presentation.ui.destination.DestinationExploreCallbacks
import com.voyager.tourism.presentation.ui.destination.DestinationExploreScreen
import com.voyager.tourism.presentation.ui.place.PlaceDetailScreen
import com.voyager.tourism.presentation.ui.preferences.TravelPreferencesScreen
import com.voyager.tourism.presentation.ui.profile.ProfileScreen
import com.voyager.tourism.presentation.ui.recommendations.RecommendationsScreen
import com.voyager.tourism.presentation.ui.settings.SettingsScreen
import com.voyager.tourism.presentation.ui.social.CommunityScreen
import com.voyager.tourism.presentation.ui.trip.CreateTravelPlanScreen
import com.voyager.tourism.presentation.ui.trip.TripDetailScreen
import com.voyager.tourism.presentation.ui.trip.TripListScreen
import com.voyager.tourism.presentation.viewmodel.AuthState
import com.voyager.tourism.presentation.viewmodel.AuthViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
                addAuthRoutes(navController, authViewModel)
                addMainAppRoutes(navController, authViewModel)
            }
        }
    }
}

private fun NavGraphBuilder.addAuthRoutes(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    composable(ROUTE_BOOTSTRAP) {
        BootstrapRoute(authState = authViewModel.authState.collectAsState().value, navController = navController)
    }
    composable(AuthViewModel.ROUTE_LOGIN) {
        LoginScreen(
            navController = navController,
            authViewModel = authViewModel,
        )
    }
    composable(AuthViewModel.ROUTE_REGISTER) {
        RegisterScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addMainAppRoutes(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    composable(AuthViewModel.ROUTE_DASHBOARD) {
        DashboardScreen(
            authViewModel = authViewModel,
            onTripClick = { tripId ->
                navController.navigate(AuthViewModel.createTripDetailRoute(tripId)) {
                    launchSingleTop = true
                }
            },
            onRecommendationsClick = {
                navController.navigate(AuthViewModel.ROUTE_RECOMMENDATIONS) {
                    launchSingleTop = true
                }
            },
            onSharedActivitiesClick = {
                navController.navigate(AuthViewModel.ROUTE_SOCIAL) {
                    launchSingleTop = true
                }
            },
            onProfileClick = {
                navController.navigate(AuthViewModel.ROUTE_PROFILE) {
                    launchSingleTop = true
                }
            },
            onSettingsClick = {
                navController.navigate(AuthViewModel.ROUTE_SETTINGS) {
                    launchSingleTop = true
                }
            },
            onPlanTripClick = {
                navController.navigate(AuthViewModel.createTravelPlanRoute("")) {
                    launchSingleTop = true
                }
            },
            onAiAssistantClick = {
                navController.navigate(AuthViewModel.ROUTE_AI_ASSISTANT) {
                    launchSingleTop = true
                }
            },
            onOpenCalendar = {
                navController.navigate(AuthViewModel.ROUTE_CALENDAR) {
                    launchSingleTop = true
                }
            },
            onCreatePlanWithDestinationHint = { hint: String ->
                navController.navigate(AuthViewModel.createTravelPlanRoute(hint)) {
                    launchSingleTop = true
                }
            },
            onDestinationExplore = { p: DestinationExploreParams ->
                val loc = p.loc.trim()
                val country = p.country?.trim().orEmpty()
                val dest = p.destId?.trim().orEmpty()
                if (loc.isBlank() && country.isBlank() && dest.isBlank()) {
                    navController.navigate(AuthViewModel.ROUTE_RECOMMENDATIONS) {
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate(
                        AuthViewModel.createDestinationExploreRoute(
                            loc = loc,
                            country = country.ifBlank { null },
                            destId = dest.ifBlank { null },
                        ),
                    ) {
                        launchSingleTop = true
                    }
                }
            },
            onViewAllTrips = {
                navController.navigate(AuthViewModel.ROUTE_TRIPS) {
                    launchSingleTop = true
                }
            },
        )
    }
    
    addTripRoutes(navController, authViewModel)
    addExploreRoutes(navController, authViewModel)
    addProfileRoutes(navController, authViewModel)
}

private fun NavGraphBuilder.addTripRoutes(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    composable(AuthViewModel.ROUTE_TRIPS) {
        TripListScreen(
            authViewModel = authViewModel,
            onTripClick = { tripId ->
                navController.navigate(AuthViewModel.createTripDetailRoute(tripId)) {
                    launchSingleTop = true
                }
            },
            onAddTrip = {
                navController.navigate(AuthViewModel.createTravelPlanRoute("")) {
                    launchSingleTop = true
                }
            },
        )
    }
    composable(
        route = AuthViewModel.ROUTE_TRIP_DETAIL,
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
    composable(
        route = AuthViewModel.ROUTE_CREATE_TRIP_WITH_HINT,
        arguments = listOf(
            navArgument("hint") {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) { entry ->
        val raw = entry.arguments?.getString("hint").orEmpty()
        val hint = try {
            URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            raw
        }
        CreateTravelPlanScreen(
            navController = navController,
            initialDestinationHint = hint,
        )
    }
}

private fun NavGraphBuilder.addExploreRoutes(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    composable(AuthViewModel.ROUTE_CALENDAR) {
        val currentUser by authViewModel.currentUser.collectAsState()
        val uid = currentUser?.id.orEmpty()
        if (uid.isNotBlank()) {
            ScheduleCalendarScreen(
                userId = uid,
                onBack = { navController.navigateUp() },
                onTripClick = { tripId ->
                    navController.navigate(AuthViewModel.createTripDetailRoute(tripId)) {
                        launchSingleTop = true
                    }
                },
                onCreatePlan = {
                    navController.navigate(AuthViewModel.createTravelPlanRoute("")) {
                        launchSingleTop = true
                    }
                },
                onViewAllTrips = {
                    navController.navigate(AuthViewModel.ROUTE_TRIPS) {
                        launchSingleTop = true
                    }
                },
            )
        } else {
            Text(
                "Inicia sesión para ver el calendario.",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
    composable(
        route = AuthViewModel.ROUTE_DESTINATION_EXPLORE,
        arguments = listOf(
            navArgument("loc") { type = NavType.StringType; defaultValue = "" },
            navArgument("country") { type = NavType.StringType; defaultValue = "" },
            navArgument("destId") { type = NavType.StringType; defaultValue = "" },
        ),
    ) { entry ->
        val loc = decodeNavQueryParam(entry.arguments?.getString("loc"))
        val country = decodeNavQueryParam(entry.arguments?.getString("country"))
        val destId = decodeNavQueryParam(entry.arguments?.getString("destId"))
        val currentUser by authViewModel.currentUser.collectAsState()
        val uid = currentUser?.id.orEmpty()
        if (uid.isNotBlank()) {
            DestinationExploreScreen(
                locRaw = loc,
                countryRaw = country,
                destIdRaw = destId,
                userId = uid,
                callbacks = DestinationExploreCallbacks(
                    onBack = { navController.navigateUp() },
                    onTripClick = { tripId ->
                        navController.navigate(AuthViewModel.createTripDetailRoute(tripId)) {
                            launchSingleTop = true
                        }
                    },
                    onCreatePlanHint = { hint: String ->
                        navController.navigate(AuthViewModel.createTravelPlanRoute(hint)) {
                            launchSingleTop = true
                        }
                    },
                ),
            )
        } else {
            Text(
                "Inicia sesión para explorar destinos.",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
    composable(AuthViewModel.ROUTE_RECOMMENDATIONS) {
        RecommendationsScreen(
            onPlaceClick = { placeId ->
                navController.navigate(
                    AuthViewModel.createDestinationExploreRoute(
                        loc = placeId,
                        country = null,
                        destId = placeId,
                    ),
                ) {
                    launchSingleTop = true
                }
            },
            onOpenAssistant = {
                navController.navigate(AuthViewModel.ROUTE_AI_ASSISTANT) {
                    launchSingleTop = true
                }
            },
        )
    }
    composable(
        route = AuthViewModel.ROUTE_PLACE_DETAIL,
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
}

private fun NavGraphBuilder.addProfileRoutes(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    composable(AuthViewModel.ROUTE_PROFILE) {
        ProfileScreen(
            onLogout = {
                authViewModel.logout()
                navController.navigate(AuthViewModel.ROUTE_LOGIN) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBack = { navController.navigateUp() },
            onSettings = {
                navController.navigate(AuthViewModel.ROUTE_SETTINGS) {
                    launchSingleTop = true
                }
            },
            onTravelPreferences = {
                navController.navigate(AuthViewModel.ROUTE_TRAVEL_PREFERENCES) {
                    launchSingleTop = true
                }
            },
        )
    }
    composable(AuthViewModel.ROUTE_SETTINGS) {
        SettingsScreen(
            navController = navController,
            onBack = { navController.navigateUp() },
        )
    }
    composable(AuthViewModel.ROUTE_BEHAVIOR_ANALYSIS) {
        val currentUser by authViewModel.currentUser.collectAsState()
        val user = currentUser
        if (user != null) {
            BehaviorAnalysisScreen(
                userId = user.id,
                onBack = { navController.navigateUp() },
            )
        } else {
            Text(
                "Inicia sesión para acceder al análisis de comportamiento.",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
    composable(AuthViewModel.ROUTE_TRAVEL_PREFERENCES) {
        val currentUser by authViewModel.currentUser.collectAsState()
        val user = currentUser
        if (user != null) {
            TravelPreferencesScreen(
                userId = user.id,
                onBack = { navController.navigateUp() },
            )
        } else {
            Text(
                "Inicia sesión para acceder a tus preferencias de viaje.",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
    composable(AuthViewModel.ROUTE_SOCIAL) {
        CommunityScreen(
            onBack = { navController.navigateUp() },
        )
    }
    composable(AuthViewModel.ROUTE_AI_ASSISTANT) {
        val currentUser by authViewModel.currentUser.collectAsState()
        val uid = currentUser?.id.orEmpty()
        if (uid.isNotBlank()) {
            AiAssistantScreen(
                onBack = { navController.navigateUp() },
            )
        } else {
            Text(
                "Inicia sesión para acceder al asistente de IA.",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

private fun decodeNavQueryParam(raw: String?): String {
    val s = raw.orEmpty()
    return try {
        URLDecoder.decode(s, StandardCharsets.UTF_8.name())
    } catch (_: Exception) {
        s
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
