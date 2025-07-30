package com.rokobanana.merx.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
) {
    val user by authViewModel.userState.collectAsState()
    var nomComplet by remember { mutableStateOf("") }
    var nomUsuari by remember { mutableStateOf("") }
    val correu = user?.correu ?: ""
    val backgroundColor = MaterialTheme.colorScheme.background
    val errorMessage by authViewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var initialNomComplet by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        nomComplet = user?.nomComplet ?: ""
        initialNomComplet = user?.nomComplet ?: ""
        nomUsuari = user?.nomUsuari ?: ""
    }

    val hasUnsavedChanges = nomComplet.trim() != initialNomComplet.trim()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tornar")
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
                Button(
                    onClick = {
                        authViewModel.updateProfile(nomComplet.trim())
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Perfil actualitzat")
                        }
                    },
                    enabled = hasUnsavedChanges,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text("Desar") }
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text("Eliminar usuari") }
                errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
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
                            authViewModel.esborrarUsuari(
                                onSuccess = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onError = { msg -> /* mostra error si vols, per exemple amb un snackbar */ }
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