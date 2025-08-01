package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.feature.material.MaterialCollectionViewModel

@Composable
fun MaterialCollectionScreen(
    grupId: String,
    viewModel: MaterialCollectionViewModel = hiltViewModel()
) {
    val collections by viewModel.collections.collectAsState()
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Carregar col·leccions al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadCollections()
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Col·leccions", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Formulari d'alta
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom de la col·lecció") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (name.isBlank()) {
                    error = "El nom no pot ser buit"
                } else {
                    isLoading = true
                    error = null
                    val newCollection = MaterialCollection(id = "", name = name)
                    viewModel.addNewCollection(newCollection) {
                        name = ""
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Guardant..." else "Afegir col·lecció")
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        // Llistat de col·leccions
        LazyColumn {
            items(collections) { collection ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(collection.name, style = MaterialTheme.typography.titleMedium)
                        if (collection.id.isNotBlank()) {
                            Text("ID: ${collection.id}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}