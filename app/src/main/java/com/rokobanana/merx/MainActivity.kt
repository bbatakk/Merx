package com.rokobanana.merx

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.rokobanana.merx.feature.afegirProducte.ui.AfegirProducteScreen
import com.rokobanana.merx.feature.autenticacio.ui.LoginScreen
import com.rokobanana.merx.feature.autenticacio.ui.RegisterScreen
import com.rokobanana.merx.feature.editarProducte.EditarProducteScreen
import com.rokobanana.merx.feature.llistaProducte.LlistaProductesScreen
import com.rokobanana.merx.feature.perfil.PerfilScreen
import com.rokobanana.merx.feature.seleccionarGrup.MenuGrupsScreen
import com.rokobanana.merx.theme.MerxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            MerxTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }
                val navController = rememberNavController()

                // Decideix quin ha de ser el startDestination
                LaunchedEffect(Unit) {
                    val user = FirebaseAuth.getInstance().currentUser
                    startDestination = if (user == null) "login" else "menuGrups"
                }

                if (startDestination != null) {
                    NavHost(navController = navController, startDestination = startDestination!!) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("menuGrups") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onBack = { navController.popBackStack() },
                                onRegisterSuccess = {
                                    navController.navigate("menuGrups") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("menuGrups") {
                            MenuGrupsScreen(navController = navController)
                        }
                        composable("llista/{grupId}") { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            LlistaProductesScreen(navController, grupId)
                        }
                        composable(
                            route = "nou/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            AfegirProducteScreen(navController = navController, grupId = grupId)
                        }
                        composable(
                            route = "detall/{grupId}/{producteId}",
                            arguments = listOf(
                                navArgument("grupId") { type = NavType.StringType },
                                navArgument("producteId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val producteId = backStackEntry.arguments?.getString("producteId") ?: ""
                            EditarProducteScreen(
                                navController = navController,
                                grupId = grupId,
                                producteId = producteId
                            )
                        }
                        composable("perfil") {
                            PerfilScreen(
                                navController = navController,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}