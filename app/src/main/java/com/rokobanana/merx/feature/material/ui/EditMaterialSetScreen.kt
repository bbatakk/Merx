package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.feature.material.EditMaterialSetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaterialSetScreen(
    grupId: String,
    setId: String?,
    onSaved: () -> Unit,
    viewModel: EditMaterialSetViewModel = hiltViewModel()
) {
    val set by viewModel.set.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQuantitat by remember { mutableStateOf("1") }

    // Carrega el set pel seu id (o crea un set nou si setId == null)
    LaunchedEffect(grupId, setId) {
        viewModel.loadSet(grupId, setId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (set.id.isEmpty()) "Nou Set de Material" else "Edita Set de Material")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Afegeix item")
            }
        },
        bottomBar = {
            Surface(shadowElevation = 6.dp) {
                Button(
                    onClick = { viewModel.saveSet { onSaved() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = set.nom.isNotBlank() && set.items.isNotEmpty() && !loading
                ) {
                    Text("Desar")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
            OutlinedTextField(
                value = set.nom,
                onValueChange = { viewModel.updateNom(it) },
                label = { Text("Nom del set") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            Text("Material", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                items(set.items) { item ->
                    MaterialItemRow(
                        item = item,
                        onUpdate = { nom, quantitat -> viewModel.updateItem(item.id, nom, quantitat) },
                        onDelete = { viewModel.removeItem(item.id) }
                    )
                }
            }
        }
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newItemName.isNotBlank() && newItemQuantitat.toIntOrNull() != null) {
                                viewModel.addItem(newItemName, newItemQuantitat.toInt())
                                newItemName = ""
                                newItemQuantitat = "1"
                                showAddDialog = false
                            }
                        }
                    ) { Text("Afegir") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddDialog = false }) { Text("Cancel·lar") }
                },
                title = { Text("Nou material") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            label = { Text("Nom") }
                        )
                        OutlinedTextField(
                            value = newItemQuantitat,
                            onValueChange = { newItemQuantitat = it },
                            label = { Text("Quantitat") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun MaterialItemRow(
    item: MaterialItem,
    onUpdate: (String, Int) -> Unit,
    onDelete: () -> Unit
) {
    var editName by remember { mutableStateOf(item.nom) }
    var editQuantitat by remember { mutableStateOf(item.quantitat.toString()) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = editName,
            onValueChange = {
                editName = it
                onUpdate(editName, editQuantitat.toIntOrNull() ?: 1)
            },
            label = { Text("Nom") },
            modifier = Modifier.weight(2f)
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = editQuantitat,
            onValueChange = {
                editQuantitat = it
                onUpdate(editName, editQuantitat.toIntOrNull() ?: 1)
            },
            label = { Text("Quantitat") },
            modifier = Modifier.width(80.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Elimina")
        }
    }
}