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
                        // --- Pantalles d'autenticació i menú ---
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { navController.navigate("menuGrups") { popUpTo("login") { inclusive = true } } },
                                onNavigateToRegister = { navController.navigate("register") },
                                authViewModel = authViewModel
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onBack = { navController.popBackStack() },
                                onRegisterSuccess = { navController.navigate("menuGrups") { popUpTo("register") { inclusive = true } } },
                                authViewModel = authViewModel
                            )
                        }
                        composable("menuGrups") {
                            MenuGrupsScreen(
                                navController = navController,
                                authViewModel = authViewModel
                            )
                        }
                        // --- Pantalles de gestió de grup ---
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
                        // --- Pantalles de Material ---
                        composable(
                            route = "colleccionsMaterial/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom by grupViewModel.getNomGrup(grupId).collectAsState(initial = "")

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
                                },
                                onEditCollection = { collection, newName ->
                                    collectionViewModel.updateCollectionName(collection, newName)
                                },
                                onDeleteCollection = { collection ->
                                    collectionViewModel.deleteCollection(collection)
                                }
                            )
                        }

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

                            val collections by collectionViewModel.collections.collectAsState()
                            val collection = collections.find { it.id == collectionId }

                            // >>>> CARREGA ELS SETS DE LA COL·LECCIÓ <<<<
                            LaunchedEffect(collection?.setIds) {
                                val setIds = collection?.setIds ?: emptyList()
                                setViewModel.loadSetsByIds(setIds)
                            }

                            val allSets by setViewModel.allSets.collectAsState()
                            val sets = allSets.filter { collection?.setIds?.contains(it.id) == true }

                            val loadedSetIds = sets.map { it.id }

                            SetsScreen(
                                sets = sets,
                                onSetClick = { set ->
                                    navController.navigate("items/${collectionId}/${set.id}?grupId=$grupId&grupNom=$grupNom")
                                },
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                collectionName = collection?.name ?: "",
                                authViewModel = authViewModel,
                                onAddSet = { nomSet ->
                                    setViewModel.addNewSet(
                                        com.rokobanana.merx.domain.model.MaterialSet(nom = nomSet)
                                    ) { newSetId ->
                                        if (collection != null) {
                                            collectionViewModel.addSetToCollection(collection.id, newSetId) {
                                                // Un cop la col·lecció s'ha actualitzat, recarrega els sets d’aquesta col·lecció!
                                                setViewModel.loadSetsByIds(collection.setIds + newSetId)
                                            }
                                        }
                                    }
                                },
                                onEditSet = { set, newName ->
                                    setViewModel.updateSetName(set, newName, loadedSetIds)
                                },
                                onDeleteSet = { set ->
                                    setViewModel.deleteSet(set, loadedSetIds)
                                }
                            )
                        }

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

                            val allSets by setViewModel.allSets.collectAsState()
                            val set = allSets.find { it.id == setId }

                            // >>>> CARREGA ELS ITEMS DEL SET <<<<
                            LaunchedEffect(set?.itemIds) {
                                val itemIds = set?.itemIds ?: emptyList()
                                itemViewModel.loadItemsByIds(itemIds)
                            }

                            val allItems by itemViewModel.allItems.collectAsState()
                            val items = allItems.filter { set?.itemIds?.contains(it.id) == true }

                            val collections by collectionViewModel.collections.collectAsState()
                            val collection = collections.find { it.id == collectionId }

                            val loadedItemIds = items.map { it.id }

                            ItemsScreen(
                                items = items,
                                onItemClick = { item ->
                                    navController.navigate("detallitem/${collectionId}/${setId}/${item.id}?grupId=$grupId&grupNom=$grupNom")
                                },
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                collectionName = collection?.name ?: "",
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
                                        if (set != null) {
                                            setViewModel.addItemToSet(set.id, itemId)
                                            // RECÀRREGA la llista d’items del set actual (inclou el nou itemId)
                                            itemViewModel.loadItemsByIds(set.itemIds + itemId)
                                        }
                                    }
                                },
                                onEditItem = { item, nom, marca, model, descripcio, quantitat ->
                                    itemViewModel.updateItem(item, nom, marca, model, descripcio, quantitat, loadedItemIds)
                                },
                                onDeleteItem = { item ->
                                    itemViewModel.deleteItem(item, loadedItemIds)
                                }
                            )
                        }
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

                            val allItems by itemViewModel.allItems.collectAsState()
                            val allSets by setViewModel.allSets.collectAsState()
                            val set = allSets.find { it.id == setId }
                            val item = allItems.find { it.id == itemId }
                            val collections by collectionViewModel.collections.collectAsState()
                            val collection = collections.find { it.id == collectionId }

                            ItemDetailScreen(
                                item = item!!,
                                navController = navController,
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                collectionName = collection?.name ?: "",
                                setName = set?.nom ?: "",
                                authViewModel = authViewModel
                            )
                        }
                        // --- Flux de càrrega de Material ---
                        composable(
                            route = "carregarMaterial/{grupId}",
                            arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val grupNom by grupViewModel.getNomGrup(grupId).collectAsState(initial = "")

                            // 1. Carrega col·leccions del grup
                            LaunchedEffect(grupId) { collectionViewModel.loadCollections(grupId) }
                            val collections by collectionViewModel.collections.collectAsState()

                            // 2. Carrega sets a partir dels setIds de les col·leccions
                            LaunchedEffect(collections) {
                                val allSetIds = collections.flatMap { it.setIds }
                                setViewModel.loadSetsByIds(allSetIds)
                            }
                            val allSets by setViewModel.allSets.collectAsState()

                            // 3. Carrega items a partir dels itemIds de tots els sets
                            LaunchedEffect(allSets) {
                                val allItemIds = allSets.flatMap { it.itemIds }
                                itemViewModel.loadItemsByIds(allItemIds)
                            }
                            val allItems by itemViewModel.allItems.collectAsState()

                            // 4. Mostra el flux de càrrega
                            LoadMaterialFlow(
                                collections = collections,
                                allSets = allSets,
                                allItems = allItems,
                                viewModel = loadMaterialViewModel,
                                onFinish = { navController.popBackStack() },
                                grupId = grupId,
                                grupNom = grupNom,
                                menuNom = "Material",
                                authViewModel = authViewModel,
                                navController = navController
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