package com.example.modernauthapp.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.modernauthapp.data.auth.AuthManager
import com.example.modernauthapp.data.model.AuthState
import com.example.modernauthapp.ui.components.AppDrawerContent
import com.example.modernauthapp.ui.screens.analytics.AnalyticsScreen
import com.example.modernauthapp.ui.screens.dashboard.DashboardScreen
import com.example.modernauthapp.ui.screens.dashboard.DashboardViewModel
import com.example.modernauthapp.ui.screens.explore.ExploreScreen
import com.example.modernauthapp.ui.screens.login.LoginScreen
import com.example.modernauthapp.ui.screens.login.LoginViewModel
import com.example.modernauthapp.ui.screens.settings.SettingsScreen
import com.example.modernauthapp.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

/**
 * Centralized NavHost orchestrating application navigation, Auth Gate,
 * ModalNavigationDrawer, and session management (backstack purging on logout).
 */
@Composable
fun AppNavHost(
    authManager: AuthManager,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val authState by authManager.authStateFlow.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.Splash.route

    // Drawer is active only on authenticated workspace screens
    val isDrawerRoute by remember(currentRoute) {
        derivedStateOf {
            currentRoute in listOf(
                NavRoutes.Dashboard.route,
                NavRoutes.Analytics.route,
                NavRoutes.Explore.route,
                NavRoutes.Settings.route
            )
        }
    }

    val currentUser = (authState as? AuthState.Authenticated)?.user ?: authManager.currentUser

    // Function to perform complete logout: purges backstack and routes to Login
    val performLogout: () -> Unit = {
        coroutineScope.launch {
            drawerState.close()
            authManager.signOut()
            // Clear entire backstack and navigate to Login
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isDrawerRoute,
        drawerContent = {
            if (isDrawerRoute) {
                AppDrawerContent(
                    user = currentUser,
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        if (targetRoute != currentRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(NavRoutes.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onLogoutClick = performLogout
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Splash.route,
            modifier = modifier
        ) {
            // Auth Gate / Splash Screen
            composable(NavRoutes.Splash.route) {
                SplashScreen(
                    authState = authState,
                    onNavigateToDashboard = {
                        navController.navigate(NavRoutes.Dashboard.route) {
                            popUpTo(NavRoutes.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(NavRoutes.Splash.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Login Screen
            composable(NavRoutes.Login.route) {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModel.Factory(authManager)
                )

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.Dashboard.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Main Dashboard Screen
            composable(NavRoutes.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(authManager)
                )

                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Demo Page 1: Analytics
            composable(NavRoutes.Analytics.route) {
                AnalyticsScreen(
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }

            // Demo Page 2: Explore
            composable(NavRoutes.Explore.route) {
                ExploreScreen(
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }

            // Settings Screen
            composable(NavRoutes.Settings.route) {
                SettingsScreen(
                    user = currentUser,
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onLogoutClick = performLogout
                )
            }
        }
    }
}
