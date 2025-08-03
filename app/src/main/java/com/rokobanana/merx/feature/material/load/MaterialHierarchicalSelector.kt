package com.rokobanana.merx.feature.material.load

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.model.MaterialItem
import androidx.compose.material3.TriStateCheckbox

@Composable
fun MaterialHierarchicalSelector(
    collections: List<MaterialCollection>,
    setsByCollection: Map<String, List<MaterialSet>>,
    itemsBySet: Map<String, List<MaterialItem>>,
    selectedItemIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    var expandedCollections by remember { mutableStateOf(setOf<String>()) }
    var expandedSets by remember { mutableStateOf(setOf<String>()) }

    fun toggleExpandedCollection(id: String) {
        expandedCollections = if (expandedCollections.contains(id)) expandedCollections - id else expandedCollections + id
    }
    fun toggleExpandedSet(id: String) {
        expandedSets = if (expandedSets.contains(id)) expandedSets - id else expandedSets + id
    }

    Column(Modifier.fillMaxWidth()) {
        collections.forEach { collection ->
            val sets = setsByCollection[collection.id].orEmpty()
            val allCollectionItemIds = sets.flatMap { itemsBySet[it.id].orEmpty() }.map { it.id }.toSet()
            val checked = selectedItemIds.containsAll(allCollectionItemIds) && allCollectionItemIds.isNotEmpty()
            val indeterminate = selectedItemIds.intersect(allCollectionItemIds).isNotEmpty() && !checked

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TriStateCheckbox(
                    state = when {
                        checked -> ToggleableState.On
                        indeterminate -> ToggleableState.Indeterminate
                        else -> ToggleableState.Off
                    },
                    onClick = {
                        val newSet =
                            if (!checked && !indeterminate) selectedItemIds + allCollectionItemIds
                            else selectedItemIds - allCollectionItemIds
                        onSelectionChange(newSet)
                    }
                )
                IconButton(onClick = { toggleExpandedCollection(collection.id) }) {
                    Icon(
                        imageVector = if (expandedCollections.contains(collection.id)) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Desplega col·lecció"
                    )
                }
                Text(collection.name, style = MaterialTheme.typography.titleMedium)
            }
            AnimatedVisibility(visible = expandedCollections.contains(collection.id)) {
                Column(Modifier.padding(start = 24.dp)) {
                    sets.forEach { set ->
                        val items = itemsBySet[set.id].orEmpty()
                        val setItemIds = items.map { it.id }.toSet()
                        val checkedSet = selectedItemIds.containsAll(setItemIds) && setItemIds.isNotEmpty()
                        val indeterminateSet = selectedItemIds.intersect(setItemIds).isNotEmpty() && !checkedSet
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TriStateCheckbox(
                                state = when {
                                    checkedSet -> ToggleableState.On
                                    indeterminateSet -> ToggleableState.Indeterminate
                                    else -> ToggleableState.Off
                                },
                                onClick = {
                                    val newSet =
                                        if (!checkedSet && !indeterminateSet) selectedItemIds + setItemIds
                                        else selectedItemIds - setItemIds
                                    onSelectionChange(newSet)
                                }
                            )
                            IconButton(onClick = { toggleExpandedSet(set.id) }) {
                                Icon(
                                    imageVector = if (expandedSets.contains(set.id)) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Desplega set"
                                )
                            }
                            Text(set.nom, style = MaterialTheme.typography.bodyLarge)
                        }
                        AnimatedVisibility(visible = expandedSets.contains(set.id)) {
                            Column(Modifier.padding(start = 24.dp)) {
                                items.forEach { item ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedItemIds.contains(item.id),
                                            onCheckedChange = { checked ->
                                                val newSet =
                                                    if (checked) selectedItemIds + item.id else selectedItemIds - item.id
                                                onSelectionChange(newSet)
                                            }
                                        )
                                        Text(item.nom, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}