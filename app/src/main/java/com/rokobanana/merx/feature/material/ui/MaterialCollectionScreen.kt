package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.rokobanana.merx.feature.autenticacio.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialCollectionScreen(
    collections: List<MaterialCollection>,
    setsByCollection: Map<String, List<MaterialSet>>,
    itemsBySet: Map<String, List<MaterialItem>>,
    navController: NavController,
    grupId: String,
    grupNom: String,
    menuNom: String,
    authViewModel: AuthViewModel,
    onAddCollection: () -> Unit,
    onAddSet: (collectionId: String, setNom: String) -> Unit,
    onAddItem: (setId: String, nom: String, marca: String?, model: String?, descripcio: String?, quantitat: Int) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Per afegir sets (ja ho tenies)
    var addingSetForCollection by remember { mutableStateOf<String?>(null) }
    var newSetName by remember { mutableStateOf("") }

    // Per afegir items
    var addingItemForSet by remember { mutableStateOf<String?>(null) }
    var itemNom by remember { mutableStateOf("") }
    var itemMarca by remember { mutableStateOf("") }
    var itemModel by remember { mutableStateOf("") }
    var itemDescripcio by remember { mutableStateOf("") }
    var itemQuantitat by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CustomDrawer(grupNom, menuNom, navController, authViewModel, grupId)
        }
    ) {
        Scaffold(
            topBar = {
                CustomTopBar(
                    grupNom = grupNom,
                    menuNom = menuNom,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    actions = {
                        IconButton(onClick = { navController.navigate("crearColleccioMaterial/$grupId") }) {
                            Icon(Icons.Default.Add, contentDescription = "Afegir col·lecció")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                collections.forEach { collection ->
                    var expandedCollection by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        onClick = { expandedCollection = !expandedCollection }
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth()) {
                                Text(collection.name, style = MaterialTheme.typography.titleMedium)
                            }
                            if (expandedCollection) {
                                val sets = setsByCollection[collection.id] ?: emptyList()
                                sets.forEach { set ->
                                    var expandedSet by remember { mutableStateOf(false) }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        onClick = { expandedSet = !expandedSet }
                                    ) {
                                        Column(Modifier.padding(10.dp)) {
                                            Row(Modifier.fillMaxWidth()) {
                                                Text(set.nom, style = MaterialTheme.typography.bodyLarge)
                                            }
                                            if (expandedSet) {
                                                val items = itemsBySet[set.id] ?: emptyList()
                                                if (items.isEmpty()) {
                                                    Text("No hi ha ítems en aquest set.", style = MaterialTheme.typography.bodyMedium)
                                                } else {
                                                    items.forEach { item ->
                                                        Text("- ${item.nom}", style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                }
                                                Spacer(Modifier.height(8.dp))

                                                // Formulari Afegir Item
                                                if (addingItemForSet == set.id) {
                                                    OutlinedTextField(
                                                        value = itemNom,
                                                        onValueChange = { itemNom = it },
                                                        label = { Text("Nom*") },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(Modifier.height(6.dp))
                                                    OutlinedTextField(
                                                        value = itemMarca,
                                                        onValueChange = { itemMarca = it },
                                                        label = { Text("Marca (opcional)") },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(Modifier.height(6.dp))
                                                    OutlinedTextField(
                                                        value = itemModel,
                                                        onValueChange = { itemModel = it },
                                                        label = { Text("Model (opcional)") },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(Modifier.height(6.dp))
                                                    OutlinedTextField(
                                                        value = itemDescripcio,
                                                        onValueChange = { itemDescripcio = it },
                                                        label = { Text("Descripció (opcional)") },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(Modifier.height(6.dp))
                                                    OutlinedTextField(
                                                        value = itemQuantitat,
                                                        onValueChange = { itemQuantitat = it.filter { c -> c.isDigit() } },
                                                        label = { Text("Quantitat*") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Number
                                                        )
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Button(
                                                        onClick = {
                                                            onAddItem(
                                                                set.id,
                                                                itemNom.trim(),
                                                                if (itemMarca.isBlank()) null else itemMarca.trim(),
                                                                if (itemModel.isBlank()) null else itemModel.trim(),
                                                                if (itemDescripcio.isBlank()) null else itemDescripcio.trim(),
                                                                itemQuantitat.toIntOrNull() ?: 1
                                                            )
                                                            // Neteja i tanca
                                                            itemNom = ""
                                                            itemMarca = ""
                                                            itemModel = ""
                                                            itemDescripcio = ""
                                                            itemQuantitat = ""
                                                            addingItemForSet = null
                                                        },
                                                        enabled = itemNom.isNotBlank() && itemQuantitat.isNotBlank(),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("Crear item")
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                    TextButton(onClick = {
                                                        itemNom = ""
                                                        itemMarca = ""
                                                        itemModel = ""
                                                        itemDescripcio = ""
                                                        itemQuantitat = ""
                                                        addingItemForSet = null
                                                    }) {
                                                        Text("Cancel·la")
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            addingItemForSet = set.id
                                                            itemNom = ""
                                                            itemMarca = ""
                                                            itemModel = ""
                                                            itemDescripcio = ""
                                                            itemQuantitat = ""
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null)
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("Afegir nou item")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))

                                // Formulari Afegir Set (ja ho tenies)
                                if (addingSetForCollection == collection.id) {
                                    OutlinedTextField(
                                        value = newSetName,
                                        onValueChange = { newSetName = it },
                                        label = { Text("Nom del nou set") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            onAddSet(collection.id, newSetName.trim())
                                            newSetName = ""
                                            addingSetForCollection = null
                                        },
                                        enabled = newSetName.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Crear set")
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(onClick = {
                                        newSetName = ""
                                        addingSetForCollection = null
                                    }) {
                                        Text("Cancel·la")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            addingSetForCollection = collection.id
                                            newSetName = ""
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Afegir nou set")
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