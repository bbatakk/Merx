package com.rokobanana.merx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.rokobanana.merx.data.DataStoreHelper
import com.rokobanana.merx.ui.afegirProducte.AfegirProducteScreen
import com.rokobanana.merx.ui.autenticacio.AuthViewModel
import com.rokobanana.merx.ui.autenticacio.AuthViewModelFactory
import com.rokobanana.merx.ui.autenticacio.LoginScreen
import com.rokobanana.merx.ui.autenticacio.RegisterScreen
import com.rokobanana.merx.ui.editarProducte.DetallProducteScreen
import com.rokobanana.merx.ui.llistaProducte.LlistaProductesScreen
import com.rokobanana.merx.ui.seleccionarGrup.SeleccionarGrupScreen
import com.rokobanana.merx.ui.theme.MerxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            MerxTheme {
                val navController = rememberNavController()
                val context = applicationContext
                var startDestination by remember { mutableStateOf<String?>(null) }
                val factory = AuthViewModelFactory(application)
                val authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

                LaunchedEffect(Unit) {
                    try {
                        val user = FirebaseAuth.getInstance().currentUser
                        val dataStoreHelper = DataStoreHelper(context)
                        val grupId = dataStoreHelper.getGrupId()

                        startDestination = when {
                            user == null -> "login"
                            grupId.isNullOrEmpty() -> "seleccio"
                            else -> "llista/$grupId"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        startDestination = "login" // fallback
                    }
                }

                // Esperem fins tenir startDestination
                if (startDestination != null) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination!!
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("seleccio") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                authViewModel = authViewModel
                            )
                        }

                        composable("register") {
                            RegisterScreen(onRegisterSuccess = {
                                navController.navigate("seleccio") {
                                    popUpTo("register") { inclusive = true }
                                }
                            })
                        }

                        composable("seleccio") {
                            SeleccionarGrupScreen(navController = navController)
                        }

                        composable(
                            route = "llista/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            LlistaProductesScreen(navController = navController, grupId = grupId)
                        }

                        composable(
                            route = "nou/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            AfegirProducteScreen(navController = navController, grupId = grupId)
                        }

                        composable(
                            route = "detall/{grupId}/{id}",
                            arguments = listOf(
                                navArgument("grupId") { type = NavType.StringType },
                                navArgument("id") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val id = backStackEntry.arguments?.getString("id") ?: ""
                            DetallProducteScreen(grupId = grupId, producteId = id, navController = navController)
                        }
                    }
                }
            }
        }
    }
}
