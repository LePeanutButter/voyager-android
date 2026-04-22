package com.voyager.tourism

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for Tourism Intelligent Platform
 * Sets up Hilt dependency injection and initializes app-wide components
 */
@HiltAndroidApp
class TourismApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here
        // e.g., logging, crash reporting, etc.
    }
}
