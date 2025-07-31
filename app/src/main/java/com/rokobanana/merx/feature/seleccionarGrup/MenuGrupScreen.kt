package com.rokobanana.merx.feature.seleccionarGrup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rokobanana.merx.feature.autenticacio.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuGrupsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: MenuGrupsViewModel = hiltViewModel()
) {
    val grups by viewModel.grups.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val errorText by viewModel.errorText.collectAsState()
    val nouNomGrup by viewModel.nouNomGrup.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val grupNoExisteix by viewModel.grupNoExisteix.collectAsState()
    val showKeyDialog by viewModel.showKeyDialog.collectAsState()
    val groupIdToJoin by viewModel.groupIdToJoin.collectAsState()
    val inputKey by viewModel.inputKey.collectAsState()
    val errorKey by viewModel.errorKey.collectAsState()
    val novaClau by viewModel.novaClau.collectAsState()
    val grupADesvincular by viewModel.grupADesvincular.collectAsState()
    val grupAEditar by viewModel.grupAEditar.collectAsState()
    val editNomGrup by viewModel.editNomGrup.collectAsState()
    val editClauGrup by viewModel.editClauGrup.collectAsState()

    BackHandler { /* No fem res */ }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú Grups") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("seleccio") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Tancar sessió")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                grups.forEach { grup ->
                    var expandedMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { navController.navigate("llista/${grup.id}") },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = grup.nom,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Box {
                                IconButton(onClick = { expandedMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opcions del grup")
                                }
                                DropdownMenu(
                                    expanded = expandedMenu,
                                    onDismissRequest = { expandedMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Editar grup") },
                                        onClick = {
                                            expandedMenu = false
                                            viewModel.grupAEditar.value = grup
                                            viewModel.editNomGrup.value = grup.nom
                                            viewModel.editClauGrup.value = ""
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar grup"
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar grup") },
                                        onClick = {
                                            expandedMenu = false
                                            viewModel.grupADesvincular.value = grup
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Eliminar grup"
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Entra a un grup existent o crea'n un de nou:",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nouNomGrup,
                    onValueChange = { viewModel.nouNomGrup.value = it },
                    label = { Text("Nom del grup") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.buscarGrupONou() },
                    enabled = nouNomGrup.isNotBlank(),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nou grup")
                }

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                if (showKeyDialog && groupIdToJoin != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetDialogs() },
                        title = { Text("Introdueix la clau") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = inputKey,
                                    onValueChange = { viewModel.inputKey.value = it },
                                    label = { Text("Clau del grup") }
                                )
                                if (errorKey != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(errorKey ?: "", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                viewModel.unirAGrup { gid ->
                                    navController.navigate("llista/$gid") {
                                        popUpTo("menuGrups") { inclusive = true }
                                    }
                                }
                            }) { Text("Entrar") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { viewModel.resetDialogs() }) { Text("Cancel·lar") }
                        }
                    )
                }

                if (showDialog && grupNoExisteix) {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetDialogs() },
                        title = { Text("Grup no existeix") },
                        text = {
                            Column {
                                Text("Aquest grup no existeix. Vols crear-lo?")
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = novaClau,
                                    onValueChange = { viewModel.novaClau.value = it },
                                    label = { Text("Clau pel nou grup") },
                                    isError = novaClau.isBlank()
                                )
                                if (novaClau.isBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Has d'introduir una clau per crear el grup.",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (novaClau.isBlank()) return@Button
                                    viewModel.crearGrup { gid ->
                                        navController.navigate("llista/$gid") {
                                            popUpTo("menuGrups") { inclusive = true }
                                        }
                                    }
                                },
                                enabled = novaClau.isNotBlank()
                            ) { Text("Sí, crear") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { viewModel.resetDialogs() }) {
                                Text("Cancel·lar")
                            }
                        }
                    )
                }

                if (grupADesvincular != null) {
                    val grup = grupADesvincular
                    AlertDialog(
                        onDismissRequest = { viewModel.grupADesvincular.value = null },
                        title = { Text("Desvincular-se del grup") },
                        text = { Text("Segur que vols desvincular-te del grup \"${grup?.nom}\"?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (grup != null) {
                                        viewModel.desvincularGrup(grup) {}
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                            ) { Text("Sí, desvincular") }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { viewModel.grupADesvincular.value = null },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Cancel·lar")}
                        }
                    )
                }

                if (grupAEditar != null) {
                    val grup = grupAEditar
                    AlertDialog(
                        onDismissRequest = {
                            viewModel.grupAEditar.value = null
                            viewModel.editNomGrup.value = ""
                            viewModel.editClauGrup.value = ""
                        },
                        title = { Text("Editar grup") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = editNomGrup,
                                    onValueChange = { viewModel.editNomGrup.value = it },
                                    label = { Text("Nom del grup") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editClauGrup,
                                    onValueChange = { viewModel.editClauGrup.value = it },
                                    label = { Text("Clau del grup (opcional)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (grup != null) {
                                        viewModel.editarGrup(grup) {}
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Desar") }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    viewModel.grupAEditar.value = null
                                    viewModel.editNomGrup.value = ""
                                    viewModel.editClauGrup.value = ""
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Cancel·lar") }
                        }
                    )
                }
            }
        }
    }
}