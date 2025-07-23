package com.rokobanana.merx.ui.seleccionarGrup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.data.DataStoreHelper
import com.rokobanana.merx.verificarOcrearGrup
import kotlinx.coroutines.launch

@Composable
fun SeleccionarGrupScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStoreHelper = remember { DataStoreHelper(context) }
    val scope = rememberCoroutineScope()

    var grupId by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Comprovar si hi ha un grupId guardat i fer la navegació automàtica
    val grupIdGuardat by dataStoreHelper.grupIdFlow.collectAsState(initial = null)

    LaunchedEffect(grupIdGuardat) {
        grupIdGuardat?.let { savedGrupId ->
            if (savedGrupId.isNotEmpty()) {
                navController.navigate("llista/$savedGrupId") {
                    popUpTo("seleccio") { inclusive = true }
                }
            }
        }
    }

    if (grupIdGuardat.isNullOrEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Entra el nom del teu grup", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = grupId,
                onValueChange = { grupId = it },
                label = { Text("Nom del grup") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    loading = true
                    error = null
                    val trimmed = grupId.trim()
                    verificarOcrearGrup(trimmed, context) { success, errorMsg ->
                        loading = false
                        if (success) {
                            scope.launch {
                                navController.navigate("llista/$trimmed") {
                                    popUpTo("seleccio") { inclusive = true }
                                }
                            }
                        } else {
                            error = errorMsg
                        }
                    }
                },
                enabled = grupId.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }



            if (loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
