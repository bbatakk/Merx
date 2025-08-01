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
import com.rokobanana.merx.feature.material.ui.EditMaterialSetScreen
import com.rokobanana.merx.feature.material.ui.MaterialSetsScreen
import com.rokobanana.merx.feature.material.ChecklistScreen // <- la pantalla de checklist
import com.rokobanana.merx.feature.perfil.PerfilScreen
import com.rokobanana.merx.feature.seleccionarGrup.MenuGrupsScreen
import com.rokobanana.merx.feature.material.MaterialSetsViewModel // <- ViewModel dels sets
import com.rokobanana.merx.theme.MerxTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokobanana.merx.feature.material.ui.MaterialCollectionScreen
import com.rokobanana.merx.feature.material.ui.MaterialSetsMultiSelectWithItemsScreen
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
                        // Crear un Set nou:
                        composable(
                            route = "nouSet/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            EditMaterialSetScreen(
                                grupId = grupId,
                                setId = null,
                                onSaved = { navController.popBackStack() }
                            )
                        }
                        composable("colleccionsMaterial/{grupId}") { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            MaterialCollectionScreen(grupId = grupId)
                        }
                        // Llistar Sets:
                        composable(
                            route = "llistaSets/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            MaterialSetsScreen(
                                grupId = grupId,
                                onEditSet = { setId ->
                                    navController.navigate("editSet/${grupId}/${setId ?: ""}")
                                }
                            )
                        }
                        // Editar Set existent (o nou si setId = null):
                        composable(
                            route = "editSet/{grupId}/{setId}",
                            arguments = listOf(
                                navArgument("grupId") { type = NavType.StringType },
                                navArgument("setId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val setId = backStackEntry.arguments?.getString("setId")
                            EditMaterialSetScreen(
                                grupId = grupId,
                                setId = setId,
                                onSaved = { navController.popBackStack() }
                            )
                        }
                        // NOVA RUTA: Selecció múltiple de sets per checklist
                        composable(
                            route = "carregarSets/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val setsViewModel: MaterialSetsViewModel = hiltViewModel()
                            val sets by setsViewModel.sets.collectAsState()
                            LaunchedEffect(grupId) { setsViewModel.loadSets(grupId) }
                            MaterialSetsMultiSelectWithItemsScreen(
                                sets = sets,
                                onStartChecklist = { selectedItems ->
                                    // Serialitza els items (per exemple, ids separats per comes)
                                    val itemIds = selectedItems.joinToString(",") { it.id }
                                    navController.navigate("checklistItems/$grupId/$itemIds")
                                }
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