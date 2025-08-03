package com.rokobanana.merx.feature.material.load

import androidx.compose.runtime.*
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.model.MaterialItem

@Composable
fun LoadMaterialFlow(
    collections: List<MaterialCollection>,
    setsByCollection: Map<String, List<MaterialSet>>,
    itemsBySet: Map<String, List<MaterialItem>>,
    viewModel: LoadMaterialViewModel,
    onFinish: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()

    when (step) {
        0 -> LoadMaterialSelectScreen(
            collections = collections,
            setsByCollection = setsByCollection,
            itemsBySet = itemsBySet,
            selectedItemIds = selectedItemIds,
            onSelectionChange = { ids -> viewModel.setSelectedItemIds(ids) },
            onStartChecklist = { step = 1 }
        )
        1 -> {
            // Obtenim la llista d’items seleccionats ordenada pel mateix ordre que les jerarquies
            val selectedItems = itemsBySet.values.flatten().filter { selectedItemIds.contains(it.id) }
            LoadMaterialChecklistScreen(
                selectedItems = selectedItems,
                checkedItems = selectedItemIds,
                onCheckItem = { itemId, checked -> viewModel.markItem(itemId, checked) },
                onFinish = {
                    viewModel.resetSession()
                    onFinish()
                    step = 0
                }
            )
        }
    }
}