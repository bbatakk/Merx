package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.model.MaterialItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSetsMultiSelectWithItemsScreen(
    sets: List<MaterialSet>,
    onStartChecklist: (List<MaterialItem>) -> Unit
) {
    // Map de setId a conjunt d'itemId seleccionats
    val selectedItems = remember { mutableStateMapOf<String, MutableSet<String>>() }
    sets.forEach { set -> if (selectedItems[set.id] == null) selectedItems[set.id] = mutableSetOf() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Selecciona material a carregar") }) },
        bottomBar = {
            val allSelectedItems = sets.flatMap { set ->
                set.items.filter { selectedItems[set.id]?.contains(it.id) == true }
            }
            Button(
                onClick = { onStartChecklist(allSelectedItems) },
                enabled = allSelectedItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { Text("Iniciar càrrega") }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(sets) { set ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Checkbox per tot el set
                        val allItemsSelected = set.items.isNotEmpty() &&
                                set.items.all { selectedItems[set.id]?.contains(it.id) == true }
                        val someItemsSelected = set.items.any { selectedItems[set.id]?.contains(it.id) == true }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(
                                checked = allItemsSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedItems[set.id]?.addAll(set.items.map { it.id })
                                    } else {
                                        selectedItems[set.id]?.clear()
                                    }
                                }
                            )
                            Text(set.nom, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.height(4.dp))
                        // Items del set
                        set.items.forEach { item ->
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 32.dp)
                            ) {
                                Checkbox(
                                    checked = selectedItems[set.id]?.contains(item.id) == true,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedItems[set.id]?.add(item.id)
                                        else selectedItems[set.id]?.remove(item.id)
                                    }
                                )
                                Text("${item.nom} (x${item.quantitat})")
                            }
                        }
                    }
                }
            }
        }
    }
}