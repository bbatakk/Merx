package com.rokobanana.merx.feature.material.load

import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.feature.autenticacio.AuthViewModel

@Composable
fun LoadMaterialFlow(
    collections: List<MaterialCollection>,
    allSets: List<MaterialSet>,
    allItems: List<MaterialItem>,
    viewModel: LoadMaterialViewModel,
    onFinish: () -> Unit,
    grupId: String,
    grupNom: String,
    menuNom: String,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val setsByCollection = remember(collections, allSets) {
        collections.associate { collection ->
            collection.id to allSets.filter { set -> collection.setIds.contains(set.id) }
        }
    }
    val itemsBySet = remember(allSets, allItems) {
        allSets.associate { set ->
            set.id to allItems.filter { item -> set.itemIds.contains(item.id) }
        }
    }

    var step by remember { mutableStateOf(0) }
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    val checkedItemIds by viewModel.checkedItemIds.collectAsState()

    when (step) {
        0 -> LoadMaterialSelectScreen(
            collections = collections,
            setsByCollection = setsByCollection,
            itemsBySet = itemsBySet,
            selectedItemIds = selectedItemIds,
            viewModel = viewModel,
            onStartChecklist = {
                viewModel.setCheckedItemIds(emptySet())
                step = 1
            },
            grupId = grupId,
            grupNom = grupNom,
            menuNom = menuNom,
            authViewModel = authViewModel,
            navController = navController
        )
        1 -> {
            // Agrupa els items seleccionats per set!
            val setsWithItems = allSets
                .map { set ->
                    set to allItems.filter { it.id in set.itemIds && it.id in selectedItemIds }
                }
                .filter { it.second.isNotEmpty() }

            LoadMaterialChecklistScreen(
                setsWithItems = setsWithItems,
                checkedItems = checkedItemIds,
                onCheckItem = { itemId, checked -> viewModel.markItem(itemId, checked) },
                onCheckSet = { itemIds, checked -> viewModel.markCheckedItems(itemIds, checked) },
                onFinish = {
                    viewModel.resetSession()
                    onFinish()
                    step = 0
                },
                grupId = grupId,
                grupNom = grupNom,
                menuNom = menuNom,
                authViewModel = authViewModel,
                navController = navController
            )
        }
    }
}