package com.hybridclassifier.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hybridclassifier.app.data.ClassifierRepository
import com.hybridclassifier.app.ui.screens.*
import com.hybridclassifier.app.ui.theme.KidsLearningTheme
import com.hybridclassifier.app.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var repository: ClassifierRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isLoggedIn = runBlocking { repository.tokenFlow.first() != null }

        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val darkMode by appViewModel.darkMode.collectAsState()
            val role     by appViewModel.role.collectAsState()

            KidsLearningTheme(darkTheme = darkMode) {
                AppNavigation(
                    startDestination = if (isLoggedIn) {
                        // If already logged in as admin go to admin, else classifier
                        if (role == "admin") "admin" else "classifier"
                    } else "login"
                )
            }
        }
    }
}

@Composable
fun AppNavigation(startDestination: String) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(tween(280))
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = tween(280)) { -it } + fadeOut(tween(280))
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn(tween(280))
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = tween(280)) { it } + fadeOut(tween(280))
        }
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    // Admin goes to admin panel, users go to classifier
                    val dest = if (role == "admin") "admin" else "classifier"
                    nav.navigate(dest) { popUpTo("login") { inclusive = true } }
                },
                onSignupClick = { nav.navigate("signup") }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupSuccess = { nav.navigate("classifier") { popUpTo("login") { inclusive = true } } },
                onBackClick = { nav.popBackStack() }
            )
        }

        composable("classifier") {
            // No admin button — admin accesses panel only through login screen
            ClassifierScreen(
                onSettingsClick = { nav.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                onLogout = { nav.navigate("login") { popUpTo(0) { inclusive = true } } }
            )
        }

        composable("admin") {
            AdminScreen(
                onBack = { nav.navigate("login") { popUpTo(0) { inclusive = true } } }
            )
        }
    }
}