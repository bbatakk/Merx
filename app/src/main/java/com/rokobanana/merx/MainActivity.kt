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
import com.rokobanana.merx.feature.material.ui.MaterialCollectionScreen
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
                            MenuGrupsScreen(
                                navController = navController
                                // Quan selecciones un grup, fes servir el GrupGlobalViewModel així:
                                // val grupGlobalViewModel: GrupGlobalViewModel = hiltViewModel()
                                // grupGlobalViewModel.setGrupId(grupIdSeleccionat)
                                // navController.navigate("llistaProductes")
                            )
                        }
                        composable(
                            route = "llista/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            navController.navigate("llistaProductes")
                        }
                        composable("llistaProductes") {
                            navController.navigate("llistaProductes")
                        }
                        composable("nouProducte") {
                            AfegirProducteScreen(navController = navController)
                        }
                        composable(
                            route = "detallProducte/{producteId}",
                            arguments = listOf(navArgument("producteId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val producteId = backStackEntry.arguments?.getString("producteId") ?: ""
                            EditarProducteScreen(producteId = producteId, navController = navController)
                        }
                        composable("perfil") {
                            PerfilScreen(
                                navController = navController,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("colleccionsMaterial") {
                            MaterialCollectionScreen()
                        }
                        // Si tens altres pantalles, afegeix-les aquí igual (sense grupId com a argument)
                        // composable("materialSets") { MaterialSetsScreen() }
                        // composable("editMaterialSet") { EditMaterialSetScreen() }
                        // composable("checklist") { ChecklistScreen() }
                        // composable("materialSetsMultiSelect") { MaterialSetsMultiSelectWithItemsScreen() }
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