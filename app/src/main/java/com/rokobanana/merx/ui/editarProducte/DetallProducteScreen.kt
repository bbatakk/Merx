package com.rokobanana.merx.ui.editarProducte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rokobanana.merx.ui.afegirProducte.ProductesViewModel
import com.rokobanana.merx.ui.afegirProducte.ProductesViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallProducteScreen(
    grupId: String,
    producteId: String,
    navController: NavController,
    viewModel: ProductesViewModel = viewModel(factory = ProductesViewModelFactory(grupId))
) {
    val producte by viewModel.getProducte(producteId).collectAsState(initial = null)
    val showDeleteDialog = remember { mutableStateOf(false) }
    val backgroundColor = MaterialTheme.colorScheme.background
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Estoc ${producte?.tipus}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Enrere"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Estoc actualitzat")
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Guardar", color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = { showDeleteDialog.value = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Eliminar")
                }
            }
        }
    ) { paddingValues ->
        producte?.let { prod ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                // Imatge destacada
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    AsyncImage(
                        model = prod.imageUrl,
                        contentDescription = prod.nom,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                        .clickable { showDialog = true }
                    )
                    if (showDialog) {
                        Dialog(onDismissRequest = { showDialog = false }) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                AsyncImage(
                                    model = prod.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDialog = false } // Tancar en clicar
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = prod.nom,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = prod.tipus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Contingut editable
                Column(modifier = Modifier.padding(16.dp)) {
                    if (prod.usaTalles) {
                        val ordreTalles = listOf("XS", "S", "M", "L", "XL", "XXL")

                        ordreTalles.forEach { talla ->
                            val quantitat = prod.estocPerTalla[talla] ?: 0
                            var valorText by remember { mutableStateOf(quantitat.toString()) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(talla, modifier = Modifier.width(40.dp))

                                Spacer(Modifier.weight(1f))

                                IconButton(
                                    onClick = {
                                        val nouValor = (valorText.toIntOrNull() ?: 0) - 1
                                        valorText = nouValor.coerceAtLeast(0).toString()
                                        viewModel.updateEstoc(prod.id, talla, nouValor.coerceAtLeast(0))
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Remove,
                                        contentDescription = "Reduir estoc",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                TextField(
                                    value = valorText,
                                    onValueChange = { newText: String ->
                                        val filtered = newText.filter { it.isDigit() }
                                        valorText = filtered
                                        viewModel.updateEstoc(prod.id, talla, filtered.toIntOrNull() ?: 0)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(56.dp),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        val nouValor = (valorText.toIntOrNull() ?: 0) + 1
                                        valorText = nouValor.toString()
                                        viewModel.updateEstoc(prod.id, talla, nouValor)
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Augmentar estoc",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        val quantitat = prod.estocPerTalla["general"] ?: 0
                        var valorText by remember { mutableStateOf(quantitat.toString()) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                "Estoc",
                                modifier = Modifier.width(60.dp),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.weight(1f))

                            IconButton(
                                onClick = {
                                    val nouValor = (valorText.toIntOrNull() ?: 0) - 1
                                    valorText = nouValor.coerceAtLeast(0).toString()
                                    viewModel.updateEstoc(prod.id, "general", nouValor.coerceAtLeast(0))
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = "Reduir estoc",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            TextField(
                                value = valorText,
                                onValueChange = { newText: String ->
                                    val filtered = newText.filter { it.isDigit() }
                                    valorText = filtered
                                    viewModel.updateEstoc(prod.id, "general", filtered.toIntOrNull() ?: 0)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(56.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            IconButton(
                                onClick = {
                                    val nouValor = (valorText.toIntOrNull() ?: 0) + 1
                                    valorText = nouValor.toString()
                                    viewModel.updateEstoc(prod.id, "general", nouValor)
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Augmentar estoc",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text("Eliminar Producte") },
                text = { Text("Segur que vols eliminar aquest producte? Aquesta acció no es pot desfer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.eliminarProducte(producteId)
                            showDeleteDialog.value = false
                            navController.popBackStack()
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog.value = false }
                    ) {
                        Text("Cancel·lar")
                    }
                }
            )
        }
    }
}
