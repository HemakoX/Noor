package com.example.drziadandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.drziadandroid.presentation.screens.HomeScreen
import com.example.drziadandroid.presentation.screens.LoginScreen
import com.example.drziadandroid.presentation.screens.PatientProfileScreen
import com.example.drziadandroid.presentation.screens.PatientsScreen
import com.example.drziadandroid.presentation.viewmodel.*
import com.example.drziadandroid.ui.theme.DrZiadAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrZiadAndroidTheme {
                val navController = rememberNavController()
                val factory = remember { ViewModelFactory() }
                val mainViewModel: MainViewModel = viewModel(factory = factory)
                val authState by mainViewModel.authState.collectAsStateWithLifecycle()

                val startDestination = remember(authState) {
                    when (authState) {
                        is AuthState.Authenticated -> "home"
                        is AuthState.Unauthenticated -> "login"
                        else -> "splash"
                    }
                }

                if (authState is AuthState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("splash") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        composable("login") {
                            val loginViewModel: LoginViewModel = viewModel(factory = factory)
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = {
                                    // The authState flow in MainViewModel will update and NavHost will recompose
                                }
                            )
                        }
                        composable("home") {
                            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
                            HomeScreen(
                                viewModel = dashboardViewModel,
                                onLogout = { mainViewModel.logout() },
                                onPatientsClick = { navController.navigate("patients") },
                                onPatientClick = { code -> navController.navigate("patient_profile/$code") }
                            )
                        }
                        composable("patients") {
                            val patientsViewModel: PatientsViewModel = viewModel(factory = factory)
                            PatientsScreen(
                                viewModel = patientsViewModel,
                                onPatientClick = { code -> navController.navigate("patient_profile/$code") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "patient_profile/{code}",
                            arguments = listOf(navArgument("code") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val code = backStackEntry.arguments?.getString("code") ?: ""
                            val profileViewModel: PatientProfileViewModel = viewModel(factory = factory)
                            PatientProfileScreen(
                                code = code,
                                viewModel = profileViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
