package com.voyager.tourism.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.voyager.tourism.presentation.navigation.TourismNavigation
import com.voyager.tourism.presentation.ui.theme.TourismTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity that hosts the navigation and serves as entry point after splash
 * Uses Hilt for dependency injection and Compose for UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TourismTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    TourismNavigation(navController = navController)
                }
            }
        }
    }
}
