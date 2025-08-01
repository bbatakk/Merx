package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.feature.material.MaterialSetsViewModel

@Composable
fun MaterialSetsScreen(
    grupId: String,
    viewModel: MaterialSetsViewModel = hiltViewModel(),
    onEditSet: (setId: String?) -> Unit = {}, // <-- ara rep un id (o null per crear)
) {
    val sets by viewModel.sets.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(grupId) {
        viewModel.loadSets(grupId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditSet(null) }) { // <-- null per crear
                Icon(Icons.Filled.Add, contentDescription = "Nou Set")
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

            LazyColumn {
                items(sets) { set -> // <-- ara directament sobre la llista
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = { onEditSet(set.id) } // <-- passa només l'id
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(set.nom, style = MaterialTheme.typography.titleLarge)
                            Text("${set.items.size} elements", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}