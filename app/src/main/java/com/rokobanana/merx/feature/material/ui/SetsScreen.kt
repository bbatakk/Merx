package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import com.rokobanana.merx.feature.material.set.MaterialSetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetsScreen(
    sets: List<MaterialSet>,
    onSetClick: (MaterialSet) -> Unit,
    navController: NavController,
    grupId: String,
    grupNom: String,
    menuNom: String,
    collectionName: String,
    authViewModel: com.rokobanana.merx.feature.autenticacio.AuthViewModel,
    onAddSet: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newSetName by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CustomDrawer(grupNom, menuNom, navController, authViewModel, grupId)
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    CustomTopBar(
                        grupNom = grupNom,
                        menuNom = menuNom,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        actions = {
                            IconButton(onClick = { showDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Afegir set")
                            }
                        }
                    )
                    BreadcrumbBar(listOf(grupNom, menuNom, collectionName, "Sets"))
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                sets.forEach { set ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        onClick = { onSetClick(set) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(set.nom, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Nou Set") },
                    text = {
                        OutlinedTextField(
                            value = newSetName,
                            onValueChange = { newSetName = it },
                            label = { Text("Nom del Set") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onAddSet(newSetName.trim())
                                newSetName = ""
                                showDialog = false
                            },
                            enabled = newSetName.isNotBlank()
                        ) {
                            Text("Crear")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel·la")
                        }
                    }
                )
            }
        }
    }
}