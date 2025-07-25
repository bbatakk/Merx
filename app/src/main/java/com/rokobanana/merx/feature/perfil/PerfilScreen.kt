package com.rokobanana.merx.feature.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rokobanana.merx.domain.model.Usuari
import com.rokobanana.merx.feature.autenticacio.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
) {
    val user by authViewModel.userState.collectAsState()
    var nomComplet by remember { mutableStateOf("") }
    var nomUsuari by remember { mutableStateOf("") }
    val correu = user?.correu ?: ""
    val errorMessage by authViewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        nomComplet = user?.nomComplet ?: ""
        nomUsuari = user?.nomUsuari ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tornar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            TextField(
                value = nomComplet,
                onValueChange = { nomComplet = it },
                label = { Text("Nom complet") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = nomUsuari,
                onValueChange = { nomUsuari = it },
                label = { Text("Nom d'usuari") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = correu,
                onValueChange = {},
                label = { Text("Correu") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    authViewModel.updateProfile(nomComplet.trim(), nomUsuari.trim())
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Desar") }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Eliminar usuari", color = MaterialTheme.colorScheme.onError) }
            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
        // Diàleg de confirmació d'esborrat
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmació") },
                text = { Text("Estàs segur que vols eliminar aquest usuari? Aquesta acció és irreversible.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            showDeleteDialog = false
                            authViewModel.deleteAccountAndUnlinkFromGroups(
                                onSuccess = { onBack() },
                                onError = { /* mostra error si vols */ }
                            )
                        }
                    ) { Text("Sí, eliminar", color = MaterialTheme.colorScheme.onError) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = false }
                    ) { Text("Cancel·lar") }
                }
            )
        }
    }
}