package com.rokobanana.merx.feature.material

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokobanana.merx.domain.model.MaterialItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    items: List<MaterialItem>,
    onFinish: () -> Unit
) {
    val checklistState = remember(items) {
        mutableStateMapOf<String, Boolean>().apply {
            items.forEach { put(it.id, false) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Checklist de càrrega") })
        },
        bottomBar = {
            Button(
                onClick = onFinish,
                enabled = checklistState.values.all { it } && checklistState.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Tot carregat!")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(items) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checklistState[item.id] == true,
                        onCheckedChange = { checked ->
                            checklistState[item.id] = checked
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${item.nom} (x${item.quantitat})")
                }
            }
        }
    }
}