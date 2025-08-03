package com.rokobanana.merx

import android.annotation.SuppressLint
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
import com.rokobanana.merx.feature.material.ui.CollectionsScreen
import com.rokobanana.merx.feature.material.ui.SetsScreen
import com.rokobanana.merx.feature.material.ui.ItemsScreen
import com.rokobanana.merx.feature.material.ui.ItemDetailScreen
import com.rokobanana.merx.feature.home.GrupHomeScreen
import com.rokobanana.merx.feature.grup.GrupViewModel
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import com.rokobanana.merx.feature.material.MaterialCollectionViewModel
import com.rokobanana.merx.feature.material.set.MaterialSetViewModel
import com.rokobanana.merx.feature.material.MaterialItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rokobanana.merx.feature.material.load.LoadMaterialFlow
import com.rokobanana.merx.feature.material.load.LoadMaterialViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("StateFlowValueCalledInComposition")
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
                val collectionViewModel: MaterialCollectionViewModel = hiltViewModel()
                val setViewModel: MaterialSetViewModel = hiltViewModel()
                val itemViewModel: MaterialItemViewModel = hiltViewModel()
                val loadMaterialViewModel: LoadMaterialViewModel = hiltViewModel()

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

                        // ------- RUTES MATERIAL -------
                        // 1. Col·leccions
                        composable(
                            route = "colleccionsMaterial/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom by grupViewModel.getNomGrup(grupId).collectAsState(initial = "")

                            // Carrega col·leccions del ViewModel
                            LaunchedEffect(grupId) { collectionViewModel.loadCollections(grupId) }
                            val collections by collectionViewModel.collections.collectAsState()

                            CollectionsScreen(
                                collections = collections,
                                onCollectionClick = { collection ->
                                    navController.navigate("sets/${collection.id}?grupId=$grupId&grupNom=$grupNom")
                                },
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                authViewModel = authViewModel,
                                onAddCollection = { nomColleccio ->
                                    collectionViewModel.addNewCollection(
                                        com.rokobanana.merx.domain.model.MaterialCollection(name = nomColleccio, grupId = grupId)
                                    )
                                }
                            )
                        }

                        // 2. Sets d'una col·lecció
                        composable(
                            route = "sets/{collectionId}?grupId={grupId}&grupNom={grupNom}",
                            arguments = listOf(
                                navArgument("collectionId") { type = NavType.StringType },
                                navArgument("grupId") { type = NavType.StringType; defaultValue = "" },
                                navArgument("grupNom") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom = backStackEntry.arguments?.getString("grupNom") ?: ""

                            LaunchedEffect(collectionId) { setViewModel.loadSets(collectionId) }
                            val sets by setViewModel.sets.collectAsState()

                            SetsScreen(
                                sets = sets,
                                onSetClick = { set ->
                                    navController.navigate("items/${collectionId}/${set.id}?grupId=$grupId&grupNom=$grupNom")
                                },
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                collectionName = collectionViewModel.collections.value.find { it.id == collectionId }?.name ?: "",
                                authViewModel = authViewModel,
                                onAddSet = { nomSet ->
                                    setViewModel.addNewSet(
                                        com.rokobanana.merx.domain.model.MaterialSet(nom = nomSet, collectionId = collectionId)
                                    )
                                }
                            )
                        }

                        // 3. Items d'un set
                        composable(
                            route = "items/{collectionId}/{setId}?grupId={grupId}&grupNom={grupNom}",
                            arguments = listOf(
                                navArgument("collectionId") { type = NavType.StringType },
                                navArgument("setId") { type = NavType.StringType },
                                navArgument("grupId") { type = NavType.StringType; defaultValue = "" },
                                navArgument("grupNom") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
                            val setId = backStackEntry.arguments?.getString("setId") ?: ""
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom = backStackEntry.arguments?.getString("grupNom") ?: ""

                            val sets by setViewModel.sets.collectAsState()
                            val set = sets.find { it.id == setId }
                            LaunchedEffect(setId) { itemViewModel.loadItems(set?.itemIds ?: emptyList()) }
                            val items by itemViewModel.items.collectAsState()

                            LaunchedEffect(set?.itemIds) {
                                if (set != null) {
                                    itemViewModel.loadItems(set.itemIds)
                                }
                            }

                            ItemsScreen(
                                items = items,
                                onItemClick = { item ->
                                    navController.navigate("detallitem/${collectionId}/${setId}/${item.id}?grupId=$grupId&grupNom=$grupNom")
                                },
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                collectionName = collectionViewModel.collections.value.find { it.id == collectionId }?.name ?: "",
                                setName = set?.nom ?: "",
                                authViewModel = authViewModel,
                                onAddItem = { nom, marca, model, descripcio, quantitat ->
                                    itemViewModel.addNewItem(
                                        com.rokobanana.merx.domain.model.MaterialItem(
                                            nom = nom,
                                            marca = marca ?: "",
                                            model = model ?: "",
                                            descripcio = descripcio ?: "",
                                            quantitat = quantitat
                                        )
                                    ) { itemId ->
                                        if (set != null) setViewModel.addItemToSet(setId, itemId)
                                    }
                                }
                            )
                        }

                        // 4. Detall d'un item
                        composable(
                            route = "detallitem/{collectionId}/{setId}/{itemId}?grupId={grupId}&grupNom={grupNom}",
                            arguments = listOf(
                                navArgument("collectionId") { type = NavType.StringType },
                                navArgument("setId") { type = NavType.StringType },
                                navArgument("itemId") { type = NavType.StringType },
                                navArgument("grupId") { type = NavType.StringType; defaultValue = "" },
                                navArgument("grupNom") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
                            val setId = backStackEntry.arguments?.getString("setId") ?: ""
                            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom = backStackEntry.arguments?.getString("grupNom") ?: ""

                            val sets by setViewModel.sets.collectAsState()
                            val items by itemViewModel.items.collectAsState()

                            val set = sets.find { it.id == setId }
                            val item = items.find { it.id == itemId }

                            ItemDetailScreen(
                                item = item!!,
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                collectionName = collectionViewModel.collections.value.find { it.id == collectionId }?.name ?: "",
                                setName = set?.nom ?: "",
                                authViewModel = authViewModel
                            )
                        }

                        composable(
                            route = "carregarMaterial/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val loadMaterialViewModel: LoadMaterialViewModel = hiltViewModel()

                            // 1. Carrega les col·leccions associades al grup
                            LaunchedEffect(grupId) {
                                collectionViewModel.loadCollections(grupId) // Assegura que carrega!
                            }
                            val collections by collectionViewModel.collections.collectAsState()

                            // 2. Carrega els sets de cada col·lecció
                            LaunchedEffect(collections) {
                                collections.forEach { col ->
                                    setViewModel.loadSets(col.id)
                                }
                            }
                            val sets by setViewModel.sets.collectAsState()

                            // 3. Agrupa els sets per col·lecció
                            val setsByCollection: Map<String, List<com.rokobanana.merx.domain.model.MaterialSet>> =
                                collections.associate { col ->
                                    col.id to sets.filter { it.collectionId == col.id }
                                }

                            // 4. Carrega els items de cada set
                            LaunchedEffect(sets) {
                                sets.forEach { set ->
                                    itemViewModel.loadItems(set.itemIds)
                                }
                            }
                            val items by itemViewModel.items.collectAsState()

                            // 5. Agrupa els items per set
                            val itemsBySet: Map<String, List<com.rokobanana.merx.domain.model.MaterialItem>> =
                                sets.associate { set ->
                                    set.id to items.filter { it.id in set.itemIds }
                                }

                            // 6. Mostra el flux
                            LoadMaterialFlow(
                                collections = collections,
                                setsByCollection = setsByCollection,
                                itemsBySet = itemsBySet,
                                viewModel = loadMaterialViewModel,
                                onFinish = { navController.popBackStack() }
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