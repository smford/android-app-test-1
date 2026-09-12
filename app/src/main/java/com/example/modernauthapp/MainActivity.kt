package com.example.modernauthapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.modernauthapp.data.auth.AuthManager
import com.example.modernauthapp.ui.navigation.AppNavHost
import com.example.modernauthapp.ui.theme.ModernAuthTheme
import com.example.modernauthapp.ui.theme.ObsidianBackground

/**
 * Main Activity hosting Jetpack Compose content with edge-to-edge enabled.
 */
class MainActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable Edge-to-Edge full bleed layout on API 24-34+
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize AuthManager singleton
        authManager = AuthManager.getInstance(applicationContext)

        setContent {
            ModernAuthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBackground
                ) {
                    AppNavHost(
                        authManager = authManager
                    )
                }
            }
        }
    }
}
