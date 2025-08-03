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
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.rokobanana.merx.feature.menuGrup.ui.MenuGrupsScreen
import com.rokobanana.merx.theme.MerxTheme
import com.rokobanana.merx.feature.material.ui.MaterialCollectionScreenWrapper
import com.rokobanana.merx.feature.home.GrupHomeScreen
import com.rokobanana.merx.feature.grup.GrupViewModel
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel

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
                val authViewModel: AuthViewModel = hiltViewModel()
                val grupViewModel: GrupViewModel = viewModel()

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
                                onNavigateToRegister = { navController.navigate("register") },
                                authViewModel = authViewModel
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onBack = { navController.popBackStack() },
                                onRegisterSuccess = {
                                    navController.navigate("menuGrups") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                authViewModel = authViewModel
                            )
                        }
                        composable("menuGrups") {
                            MenuGrupsScreen(
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }
                        composable(
                            route = "grupHome/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom by grupViewModel.getNomGrup(grupId).collectAsState(initial = "")
                            GrupHomeScreen(
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Inici",
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }
                        composable(
                            route = "llistaProductes/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom by grupViewModel.getNomGrup(grupId).collectAsState(initial = "")
                            LlistaProductesScreen(
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Gestió d'Estoc",
                                authViewModel = authViewModel,
                                productesViewModel = hiltViewModel()
                            )
                        }
                        composable(
                            route = "nouProducte/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom by grupViewModel.getNomGrup(grupId).collectAsState(initial = "")
                            AfegirProducteScreen(
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Afegir Producte",
                                authViewModel = authViewModel
                            )
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
                            val grupNom by grupViewModel.getNomGrup(grupId).collectAsState(initial = "")
                            EditarProducteScreen(
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                producteId = producteId,
                                menuNom = "Editar Producte",
                                authViewModel = authViewModel
                            )
                        }
                        composable("perfil") {
                            PerfilScreen(
                                navController = navController,
                                onBack = { navController.popBackStack() },
                                authViewModel = authViewModel
                            )
                        }
                        composable(
                            route = "colleccionsMaterial/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom by grupViewModel.getNomGrup(grupId).collectAsState(initial = "")
                            MaterialCollectionScreenWrapper(
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }
                        
                        // ... altres rutes amb grupId si cal
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