package com.rokobanana.merx.feature.material.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    collections: List<MaterialCollection>,
    onCollectionClick: (MaterialCollection) -> Unit,
    navController: NavController,
    grupId: String,
    grupNom: String,
    menuNom: String,
    authViewModel: AuthViewModel,
    onAddCollection: (String) -> Unit,
    onEditCollection: (MaterialCollection, String) -> Unit,
    onDeleteCollection: (MaterialCollection) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }
    var editDialogCollection by remember { mutableStateOf<MaterialCollection?>(null) }
    var editCollectionName by remember { mutableStateOf("") }
    var deleteDialogCollection by remember { mutableStateOf<MaterialCollection?>(null) }

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
                        IconButton(onClick = { showDialog = true }) {
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
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    "Col·leccions de material:",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(collections.size) { idx ->
                        val collection = collections[idx]
                        var expandedMenu by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable { onCollectionClick(collection) },
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
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Col·lecció",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                                Text(
                                    collection.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 14.dp)
                                )
                                Spacer(Modifier.weight(1f))
                                Box {
                                    IconButton(onClick = { expandedMenu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opcions de la col·lecció")
                                    }
                                    DropdownMenu(
                                        expanded = expandedMenu,
                                        onDismissRequest = { expandedMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Editar") },
                                            onClick = {
                                                expandedMenu = false
                                                editDialogCollection = collection
                                                editCollectionName = collection.name
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar") },
                                            onClick = {
                                                expandedMenu = false
                                                deleteDialogCollection = collection
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Diàleg afegir
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Nova col·lecció") },
                    text = {
                        OutlinedTextField(
                            value = newCollectionName,
                            onValueChange = { newCollectionName = it },
                            label = { Text("Nom de la col·lecció") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onAddCollection(newCollectionName.trim())
                                newCollectionName = ""
                                showDialog = false
                            },
                            enabled = newCollectionName.isNotBlank()
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
            // Diàleg editar
            if (editDialogCollection != null) {
                AlertDialog(
                    onDismissRequest = { editDialogCollection = null },
                    title = { Text("Editar col·lecció") },
                    text = {
                        OutlinedTextField(
                            value = editCollectionName,
                            onValueChange = { editCollectionName = it },
                            label = { Text("Nom de la col·lecció") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                editDialogCollection?.let { onEditCollection(it, editCollectionName.trim()) }
                                editDialogCollection = null
                            },
                            enabled = editCollectionName.isNotBlank()
                        ) { Text("Desar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { editDialogCollection = null }) { Text("Cancel·la") }
                    }
                )
            }
            // Diàleg eliminar
            if (deleteDialogCollection != null) {
                AlertDialog(
                    onDismissRequest = { deleteDialogCollection = null },
                    title = { Text("Eliminar col·lecció") },
                    text = { Text("Estàs segur que vols eliminar la col·lecció \"${deleteDialogCollection?.name}\"?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                deleteDialogCollection?.let { onDeleteCollection(it) }
                                deleteDialogCollection = null
                            }
                        ) { Text("Eliminar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteDialogCollection = null }) { Text("Cancel·la") }
                    }
                )
            }
        }
    }
}