package com.rokobanana.merx.feature.material.load

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoadMaterialChecklistScreen(
    setsWithItems: List<Pair<MaterialSet, List<MaterialItem>>>,
    checkedItems: Set<String>,
    onCheckItem: (String, Boolean) -> Unit,
    onCheckSet: (Set<String>, Boolean) -> Unit,
    onFinish: () -> Unit,
    grupId: String,
    grupNom: String,
    menuNom: String,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var expandedSets by remember { mutableStateOf(setsWithItems.map { it.first.id }.toSet()) }

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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Marca tot el material que has carregat:",
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
                    setsWithItems.forEach { (set, items) ->
                        val setItemIds = items.map { it.id }.toSet()
                        val checkedSet = setItemIds.isNotEmpty() && checkedItems.containsAll(setItemIds)
                        val indeterminateSet = setItemIds.isNotEmpty() &&
                                checkedItems.intersect(setItemIds).isNotEmpty() &&
                                !checkedSet

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
                                    // Header del set
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedSets = if (expandedSets.contains(set.id))
                                                    expandedSets - set.id
                                                else
                                                    expandedSets + set.id
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                                onCheckSet(setItemIds, checkedNow)
                                            }
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Backpack,
                                            contentDescription = "Set",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(start = 4.dp)
                                        )
                                        Text(
                                            set.nom,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (expandedSets.contains(set.id)) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    AnimatedVisibility(visible = expandedSets.contains(set.id)) {
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = MaterialTheme.colorScheme.background,
                                                    shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
                                                )
                                                .padding(start = 12.dp, end = 8.dp, bottom = 8.dp)
                                        ) {
                                            items.forEach { item ->
                                                Row(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = checkedItems.contains(item.id),
                                                        onCheckedChange = { checked ->
                                                            onCheckItem(item.id, checked)
                                                        },
                                                        colors = CheckboxDefaults.colors(
                                                            checkedColor = MaterialTheme.colorScheme.primary,
                                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    )
                                                    Text(
                                                        item.nom,
                                                        style = MaterialTheme.typography.bodyLarge,
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
                val allItemIds = setsWithItems.flatMap { it.second }.map { it.id }
                if (allItemIds.isNotEmpty() && allItemIds.all { checkedItems.contains(it) }) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Ja pots marxar! Tot el material ha estat carregat.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Finalitzar", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}