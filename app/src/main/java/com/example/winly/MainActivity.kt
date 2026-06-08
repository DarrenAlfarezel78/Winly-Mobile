package com.example.winly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.winly.data.SessionManager
import com.example.winly.ui.auth.*
import com.example.winly.ui.home.CreateCompetitionScreen
import com.example.winly.ui.home.DetailLombaScreen
import com.example.winly.ui.home.HomeScreen
import com.example.winly.ui.home.KelolaPendaftarScreen
import com.example.winly.ui.home.BookmarkScreen
import com.example.winly.NotificationHelper
import com.example.winly.ui.theme.WinlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inisialisasi notification channel
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)

        setContent {
            WinlyTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    val navController = rememberNavController()

                    val startDestination = if (sessionManager.isLoggedIn()) {
                        "home/${sessionManager.getRole()}"
                    } else {
                        "greeting"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {

                        // 1. GREETING
                        composable("greeting") {
                            GreetingScreen(onEnterArena = { navController.navigate("auth_selection") })
                        }

                        // 2. AUTH SELECTION
                        composable("auth_selection") {
                            AuthSelectionScreen(
                                onLoginClick = { navController.navigate("login") },
                                onRegisterClick = { navController.navigate("register_step_1") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 3. LOGIN
                        composable("login") {
                            LoginScreen(
                                onBack = { navController.navigate("auth_selection") },
                                onForgotPasswordClick = { navController.navigate("forgot_password") },
                                onRegisterClick = { navController.navigate("register_step_1") },
                                onLoginSuccess = { role ->
                                    navController.navigate("home/$role") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. HOME
                        composable(
                            route = "home/{userRole}",
                            arguments = listOf(navArgument("userRole") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("userRole") ?: "peserta"
                            HomeScreen(
                                userRole = role,
                                onNavigateToCreate = { navController.navigate("create_competition") },
                                onNavigateToDetail = { id -> navController.navigate("detail_lomba/$id") },

                                // TAMBAHKAN BAGIAN INI DI SINI
                                onNavigateToKelola = { id, judul ->
                                    val encodedJudul = java.net.URLEncoder.encode(judul, "UTF-8")
                                    navController.navigate("kelola_pendaftar/$id/$encodedJudul")
                                },

                                onLogout = {
                                    sessionManager.clearSession()
                                    navController.navigate("greeting") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 5. DETAIL LOMBA
                        composable(
                            route = "detail_lomba/{competitionId}",
                            arguments = listOf(navArgument("competitionId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("competitionId") ?: 0
                            DetailLombaScreen(
                                competitionId = id,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // 6. REGISTER STEP 1
                        composable("register_step_1") {
                            RegisterScreen(
                                onBack = { navController.popBackStack() },
                                onNext = { username, role, instansi ->
                                    // Encode instansi supaya aman di URL
                                    val encodedInstansi = java.net.URLEncoder.encode(instansi, "UTF-8")
                                    val encodedUsername = java.net.URLEncoder.encode(username, "UTF-8")
                                    navController.navigate("register_step_2/$encodedUsername/$role/$encodedInstansi")
                                }
                            )
                        }

                        // 7. REGISTER STEP 2
                        composable(
                            route = "register_step_2/{userName}/{userRole}/{instansi}",
                            arguments = listOf(
                                navArgument("userName") { type = NavType.StringType },
                                navArgument("userRole") { type = NavType.StringType },
                                navArgument("instansi") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val name     = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("userName") ?: "", "UTF-8")
                            val role     = backStackEntry.arguments?.getString("userRole") ?: "peserta"
                            val instansi = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("instansi") ?: "", "UTF-8")
                            RegisterStepTwoScreen(
                                userName     = name,
                                selectedRole = role,
                                instansi     = instansi,
                                onBack       = { navController.popBackStack() },
                                onSignUp     = { email ->
                                    navController.navigate("register_step_3/$email")
                                }
                            )
                        }

                        // 8. REGISTER STEP 3
                        composable(
                            route = "register_step_3/{userEmail}",
                            arguments = listOf(navArgument("userEmail") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("userEmail") ?: ""
                            RegisterStepThreeScreen(
                                email = email,
                                onBack = { navController.popBackStack() },
                                onVerifySuccess = {
                                    navController.navigate("login") {
                                        popUpTo("greeting") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // KELOLA PENDAFTAR
                        composable(
                            route = "kelola_pendaftar/{competitionId}/{judulLomba}",
                            arguments = listOf(
                                navArgument("competitionId") { type = NavType.IntType },
                                navArgument("judulLomba") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val compId = backStackEntry.arguments?.getInt("competitionId") ?: 0
                            val judul  = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("judulLomba") ?: "", "UTF-8")
                            KelolaPendaftarScreen(
                                competitionId = compId,
                                judulLomba    = judul,
                                onBack        = { navController.popBackStack() }
                            )
                        }

                        // 9. CREATE COMPETITION
                        composable("create_competition") {
                            CreateCompetitionScreen(
                                onBack = { navController.popBackStack() },
                                onUploadSuccess = { navController.popBackStack() }
                            )
                        }

                        // 10. FORGOT PASSWORD
                        composable("forgot_password") {
                            ForgotPasswordScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}