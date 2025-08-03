package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    items: List<MaterialItem>,
    onItemClick: (MaterialItem) -> Unit,
    navController: NavController,
    grupId: String,
    grupNom: String,
    menuNom: String,
    collectionName: String,
    setName: String,
    authViewModel: AuthViewModel,
    onAddItem: (nom: String, marca: String?, model: String?, descripcio: String?, quantitat: Int) -> Unit,
    refreshItems: (() -> Unit)? = null // Afegit per refrescar la llista si cal
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemMarca by remember { mutableStateOf("") }
    var newItemModel by remember { mutableStateOf("") }
    var newItemDescripcio by remember { mutableStateOf("") }
    var newItemQuantitat by remember { mutableStateOf("") }

    // Controlar la recàrrega després de crear un item
    var created by remember { mutableStateOf(false) }
    LaunchedEffect(created) {
        if (created) {
            refreshItems?.invoke()
            created = false
        }
    }

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
                                Icon(Icons.Default.Add, contentDescription = "Afegir item")
                            }
                        }
                    )
                    BreadcrumbBar(listOf(menuNom, collectionName, setName))
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        onClick = { onItemClick(item) }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(item.nom, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Nou Item") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newItemName,
                                onValueChange = { newItemName = it },
                                label = { Text("Nom de l'item") }
                            )
                            OutlinedTextField(
                                value = newItemMarca,
                                onValueChange = { newItemMarca = it },
                                label = { Text("Marca") }
                            )
                            OutlinedTextField(
                                value = newItemModel,
                                onValueChange = { newItemModel = it },
                                label = { Text("Model") }
                            )
                            OutlinedTextField(
                                value = newItemDescripcio,
                                onValueChange = { newItemDescripcio = it },
                                label = { Text("Descripció") }
                            )
                            OutlinedTextField(
                                value = newItemQuantitat,
                                onValueChange = { newItemQuantitat = it },
                                label = { Text("Quantitat") }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val quant = newItemQuantitat.toIntOrNull() ?: 1
                                onAddItem(
                                    newItemName.trim(),
                                    newItemMarca.trim().ifBlank { null },
                                    newItemModel.trim().ifBlank { null },
                                    newItemDescripcio.trim().ifBlank { null },
                                    quant
                                )
                                newItemName = ""
                                newItemMarca = ""
                                newItemModel = ""
                                newItemDescripcio = ""
                                newItemQuantitat = ""
                                showDialog = false
                                created = true // Marca que s'ha creat
                            },
                            enabled = newItemName.isNotBlank() && newItemQuantitat.isNotBlank()
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