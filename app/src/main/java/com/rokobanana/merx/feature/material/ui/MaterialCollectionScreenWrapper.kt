package com.rokobanana.merx.feature.material.ui

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.rokobanana.merx.feature.material.MaterialCollectionViewModel
import com.rokobanana.merx.feature.material.set.MaterialSetViewModel
import com.rokobanana.merx.feature.material.MaterialItemViewModel
import com.rokobanana.merx.feature.autenticacio.AuthViewModel

@Composable
fun MaterialCollectionScreenWrapper(
    grupId: String,
    grupNom: String,
    menuNom: String,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val collectionVM: MaterialCollectionViewModel = hiltViewModel()
    val setVM: MaterialSetViewModel = hiltViewModel()
    val itemVM: MaterialItemViewModel = hiltViewModel()

    // Carrega col·leccions al canvi de grupId
    LaunchedEffect(grupId) { collectionVM.loadCollections(grupId) }

    val collections by collectionVM.collections.collectAsState()

    CollectionsScreen(
        collections = collections,
        onCollectionClick = { collection ->
            navController.navigate("collections/${collection.id}/sets")
        },
        navController = navController,
        grupId = grupId,
        grupNom = grupNom,
        menuNom = menuNom,
        authViewModel = authViewModel,
        onAddCollection = { nomColleccio ->
            collectionVM.addNewCollection(
                com.rokobanana.merx.domain.model.MaterialCollection(name = nomColleccio, grupId = grupId)
            )
        },
        onEditCollection = { collection, newName ->
            collectionVM.updateCollectionName(collection, newName)
        },
        onDeleteCollection = { collection ->
            collectionVM.deleteCollection(collection)
        }
    )
}