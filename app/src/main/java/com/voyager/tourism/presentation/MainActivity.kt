package com.voyager.tourism.presentation

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.voyager.tourism.presentation.navigation.TourismNavigation
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity that hosts the navigation and serves as entry point after splash
 * Uses Hilt for dependency injection and Compose for UI
 * Handles Google OAuth2 deep link callbacks
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check for Google OAuth2 callback on app launch
        handleGoogleCallback(intent?.data)
        
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                TourismNavigation(navController = navController)
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        // Handle Google OAuth2 callback when app is already running
        handleGoogleCallback(intent?.data)
    }
    
    /**
     * Handle Google OAuth2 callback from deep link
     */
    private fun handleGoogleCallback(uri: Uri?) {
        uri?.let {
            if (uri.scheme == "smartrip" && uri.host == "auth") {
                // Extract authorization code and state from the callback
                val code = uri.getQueryParameter("code")
                val error = uri.getQueryParameter("error")
                
                if (code != null) {
                    // Navigate to login screen with the callback data
                    // The LoginViewModel will handle the token exchange
                    // This is a simplified approach - in a production app,
                    // you might want to use a more sophisticated callback handling
                    // mechanism like a shared ViewModel or event bus
                } else if (error != null) {
                    // Handle OAuth error
                    // You could show an error message or navigate to an error screen
                }
            }
        }
    }
}
