package com.rokobanana.merx.feature.material.load

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadMaterialSelectScreen(
    state: LoadMaterialState,
    onToggleMaterial: (String) -> Unit,
    onProceedToChecklist: () -> Unit,
    navController: NavController,
    grupId: String,
    grupNom: String,
    authViewModel: AuthViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CustomDrawer("Carregar Material", "Seleccionar Material", navController, authViewModel, grupId)
        }
    ) {
        Scaffold(
            topBar = {
                CustomTopBar(
                    grupNom = grupNom,
                    menuNom = "Carregar Material",
                    onMenuClick = { scope.launch { drawerState.open() } },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Tornar")
                        }
                    }
                )
            },
            bottomBar = {
                if (state.selectedMaterials.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.selectedMaterials.size} ítems seleccionats",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = onProceedToChecklist,
                                enabled = state.selectedMaterials.isNotEmpty()
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Continuar")
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Selecciona el material necessari",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Marca els ítems que necessitaràs per al bolo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                // Material list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.availableMaterials) { material ->
                        MaterialSelectionItem(
                            material = material,
                            isSelected = state.selectedMaterials.contains(material),
                            onToggle = { onToggleMaterial(material.id) }
                        )
                    }
                    
                    // Add some bottom padding for the bottom bar
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialSelectionItem(
    material: MaterialItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = material.nom,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (material.marca.isNotBlank() || material.model.isNotBlank()) {
                    Text(
                        text = buildString {
                            if (material.marca.isNotBlank()) append(material.marca)
                            if (material.marca.isNotBlank() && material.model.isNotBlank()) append(" ")
                            if (material.model.isNotBlank()) append(material.model)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (material.descripcio.isNotBlank()) {
                    Text(
                        text = material.descripcio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (material.quantitat > 1) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${material.quantitat}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}