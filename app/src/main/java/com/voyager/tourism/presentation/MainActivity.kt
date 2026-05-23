package com.voyager.tourism.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.presentation.navigation.TourismNavigation
import com.voyager.tourism.presentation.ui.theme.SmarTripTheme
import com.voyager.tourism.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Host activity for the Compose navigation graph and Google OAuth return URL handling.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager

    /**
     * Builds the Compose UI root and nav host; forwards the initial intent for OAuth callbacks.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dispatchOAuthIntent(intent)
        setContent {
            val darkTheme by preferencesManager.darkThemeFlow.collectAsState()
            SmarTripTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    TourismNavigation(
                        navController = navController,
                        authViewModel = authViewModel,
                    )
                }
            }
        }
    }

    /**
     * Rebinds [Intent] on single-top launches and re-dispatches OAuth deep links.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchOAuthIntent(intent)
    }

    /**
     * Routes `smartrip://auth` callback URLs into [AuthViewModel.handleGoogleOAuthUri].
     */
    private fun dispatchOAuthIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "smartrip" && uri.host == "auth") {
            authViewModel.handleGoogleOAuthUri(uri)
        }
    }
}
