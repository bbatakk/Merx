package com.rokobanana.merx.feature.material.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    refreshItems: (() -> Unit)? = null
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemMarca by remember { mutableStateOf("") }
    var newItemModel by remember { mutableStateOf("") }
    var newItemDescripcio by remember { mutableStateOf("") }
    var newItemQuantitat by remember { mutableStateOf("") }

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
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    "Items del set:",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(items.size) { idx ->
                        val item = items[idx]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable { onItemClick(item) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2, // o qualsevol icona Material adequada
                                    contentDescription = "Item",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(25.dp)
                                )
                                Text(
                                    item.nom,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 14.dp)
                                )
                            }
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
                                created = true
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