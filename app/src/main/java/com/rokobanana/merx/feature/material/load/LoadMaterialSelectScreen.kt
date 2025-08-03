package com.rokobanana.merx.feature.material.load

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun LoadMaterialSelectScreen(
    collections: List<MaterialCollection>,
    setsByCollection: Map<String, List<MaterialSet>>,
    itemsBySet: Map<String, List<MaterialItem>>,
    selectedItemIds: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    onStartChecklist: () -> Unit,
    grupId: String,
    grupNom: String,
    menuNom: String,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                MaterialHierarchicalSelector(
                    collections = collections,
                    setsByCollection = setsByCollection,
                    itemsBySet = itemsBySet,
                    selectedItemIds = selectedItemIds,
                    onSelectionChange = onSelectionChange
                )
                Spacer(Modifier.height(24.dp))
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