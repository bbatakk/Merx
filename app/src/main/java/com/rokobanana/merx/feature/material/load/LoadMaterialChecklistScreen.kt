package com.rokobanana.merx.feature.material.load

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rokobanana.merx.domain.model.MaterialItem

@Composable
fun LoadMaterialChecklistScreen(
    selectedItems: List<MaterialItem>,
    checkedItems: Set<String>,
    onCheckItem: (String, Boolean) -> Unit,
    onFinish: () -> Unit
) {
    val allChecked = selectedItems.isNotEmpty() && selectedItems.all { checkedItems.contains(it.id) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Checklist de càrrega:", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(selectedItems.size) { idx ->
                val item = selectedItems[idx]
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkedItems.contains(item.id),
                        onCheckedChange = { onCheckItem(item.id, it) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(item.nom)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        if (allChecked) {
            Text(
                "Ja pots marxar! Tot el material ha estat carregat.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finalitzar")
            }
        }
    }
}