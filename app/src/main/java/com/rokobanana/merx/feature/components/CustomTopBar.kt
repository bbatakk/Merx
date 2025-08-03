package com.rokobanana.merx.feature.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    grupNom: String,
    menuNom: String,
    onMenuClick: () -> Unit,
    actions: (@Composable () -> Unit)? = null // <-- Afegeix aquest paràmetre!
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = grupNom,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (menuNom.isNotEmpty()) {
                    Text(
                        text = "  |  $menuNom",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Obre menú")
            }
        },
        actions = {
            actions?.invoke()
        }
    )
}