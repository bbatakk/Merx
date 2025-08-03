package com.rokobanana.merx.feature.material.load

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.model.MaterialItem

@Composable
fun LoadMaterialSelectScreen(
    collections: List<MaterialCollection>,
    setsByCollection: Map<String, List<MaterialSet>>,
    itemsBySet: Map<String, List<MaterialItem>>,
    selectedItemIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onStartChecklist: () -> Unit
) {
    Scaffold(
        topBar = { /* CustomTopBar si vols */ }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selecciona el material que vols carregar:", style = MaterialTheme.typography.titleLarge)
            MaterialHierarchicalSelector(
                collections = collections,
                setsByCollection = setsByCollection,
                itemsBySet = itemsBySet,
                selectedItemIds = selectedItemIds,
                onSelectionChange = onSelectionChange
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onStartChecklist,
                enabled = selectedItemIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar càrrega")
            }
        }
    }
}