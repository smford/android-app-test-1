package com.example.modernauthapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Type-safe route definitions for Jetpack Navigation Compose.
 */
sealed class NavRoutes(val route: String) {
    data object Splash : NavRoutes("splash")
    data object Login : NavRoutes("login")
    data object Dashboard : NavRoutes("dashboard")
    data object Analytics : NavRoutes("analytics")
    data object Explore : NavRoutes("explore")
    data object Settings : NavRoutes("settings")
}

/**
 * Drawer navigation items model.
 */
data class DrawerMenuItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

val DrawerNavigationItems = listOf(
    DrawerMenuItem(title = "Home", route = NavRoutes.Dashboard.route, icon = Icons.Outlined.Home),
    DrawerMenuItem(title = "Demo Page 1 (Analytics)", route = NavRoutes.Analytics.route, icon = Icons.Outlined.Analytics),
    DrawerMenuItem(title = "Demo Page 2 (Explore)", route = NavRoutes.Explore.route, icon = Icons.Outlined.Explore),
    DrawerMenuItem(title = "Settings", route = NavRoutes.Settings.route, icon = Icons.Outlined.Settings)
)
