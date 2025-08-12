package com.rokobanana.merx.feature.material.load

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoadMaterialSelectScreen(
    collections: List<MaterialCollection>,
    setsByCollection: Map<String, List<MaterialSet>>,
    itemsBySet: Map<String, List<MaterialItem>>,
    selectedItemIds: Set<String>,
    viewModel: LoadMaterialViewModel,
    onStartChecklist: () -> Unit,
    grupId: String,
    grupNom: String,
    menuNom: String,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var expandedCollections by remember { mutableStateOf(setOf<String>()) }
    var expandedSets by remember { mutableStateOf(setOf<String>()) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CustomDrawer(
                grupNom = grupNom,
                menuNom = menuNom,
                navController = navController,
                authViewModel = authViewModel,
                grupId = grupId
            )
        }
    ) {
        Scaffold(
            topBar = {
                CustomTopBar(
                    grupNom = grupNom,
                    menuNom = menuNom,
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Selecciona el material que vols carregar:",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    collections.forEach { collection ->
                        val sets = setsByCollection[collection.id].orEmpty()
                        val allCollectionItemIds = sets.flatMap { itemsBySet[it.id].orEmpty() }.map { it.id }.toSet()
                        val checked = allCollectionItemIds.isNotEmpty() && selectedItemIds.containsAll(allCollectionItemIds)
                        val indeterminate = allCollectionItemIds.isNotEmpty() && selectedItemIds.intersect(allCollectionItemIds).isNotEmpty() && !checked

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedCollections = if (expandedCollections.contains(collection.id))
                                                    expandedCollections - collection.id
                                                else
                                                    expandedCollections + collection.id
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TriStateCheckbox(
                                            state = when {
                                                checked -> ToggleableState.On
                                                indeterminate -> ToggleableState.Indeterminate
                                                else -> ToggleableState.Off
                                            },
                                            onClick = {
                                                val checkedNow = !checked && !indeterminate
                                                viewModel.markItems(allCollectionItemIds, checkedNow)
                                            }
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = "Col·lecció",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(start = 4.dp)
                                        )
                                        Text(
                                            collection.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (expandedCollections.contains(collection.id)) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    AnimatedVisibility(visible = expandedCollections.contains(collection.id)) {
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = MaterialTheme.colorScheme.background,
                                                    shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
                                                )
                                                .padding(start = 12.dp, end = 8.dp, bottom = 8.dp)
                                        ) {
                                            sets.forEach { set ->
                                                val items = itemsBySet[set.id].orEmpty()
                                                val setItemIds = items.map { it.id }.toSet()
                                                val checkedSet = setItemIds.isNotEmpty() && selectedItemIds.containsAll(setItemIds)
                                                val indeterminateSet = setItemIds.isNotEmpty() && selectedItemIds.intersect(setItemIds).isNotEmpty() && !checkedSet

                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .animateContentSize()
                                                        .padding(bottom = 8.dp),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
                                                    ),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                ) {
                                                    Column {
                                                        Row(
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    expandedSets = if (expandedSets.contains(set.id))
                                                                        expandedSets - set.id
                                                                    else
                                                                        expandedSets + set.id
                                                                }
                                                                .padding(horizontal = 10.dp, vertical = 10.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            TriStateCheckbox(
                                                                state = when {
                                                                    checkedSet -> ToggleableState.On
                                                                    indeterminateSet -> ToggleableState.Indeterminate
                                                                    else -> ToggleableState.Off
                                                                },
                                                                onClick = {
                                                                    val checkedNow = !checkedSet && !indeterminateSet
                                                                    viewModel.markItems(setItemIds, checkedNow)
                                                                }
                                                            )
                                                            Icon(
                                                                imageVector = Icons.Default.Backpack,
                                                                contentDescription = "Set",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                            Text(
                                                                set.nom,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                fontWeight = FontWeight.SemiBold,
                                                                modifier = Modifier
                                                                    .padding(start = 10.dp)
                                                                    .weight(1f)
                                                            )
                                                            Icon(
                                                                imageVector = if (expandedSets.contains(set.id)) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                        AnimatedVisibility(visible = expandedSets.contains(set.id)) {
                                                            Column(
                                                                Modifier
                                                                    .fillMaxWidth()
                                                                    .background(
                                                                        color = MaterialTheme.colorScheme.background,
                                                                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                                                                    )
                                                                    .padding(start = 18.dp, end = 4.dp, bottom = 8.dp)
                                                            ) {
                                                                items.forEach { item ->
                                                                    Row(
                                                                        Modifier
                                                                            .fillMaxWidth()
                                                                            .padding(vertical = 5.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Checkbox(
                                                                            checked = selectedItemIds.contains(item.id),
                                                                            onCheckedChange = { checked ->
                                                                                viewModel.markItems(setOf(item.id), checked)
                                                                            },
                                                                            colors = CheckboxDefaults.colors(
                                                                                checkedColor = MaterialTheme.colorScheme.primary,
                                                                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                                            )
                                                                        )
                                                                        Text(
                                                                            item.nom,
                                                                            style = MaterialTheme.typography.bodyMedium,
                                                                            color = MaterialTheme.colorScheme.onSurface,
                                                                            modifier = Modifier.padding(start = 8.dp)
                                                                        )
                                                                    }
                                                                    Divider(
                                                                        thickness = 0.7.dp,
                                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                                    )
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
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onStartChecklist,
                    enabled = selectedItemIds.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Iniciar càrrega", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}