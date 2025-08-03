package com.rokobanana.merx.feature.material.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    item: MaterialItem,
    navController: NavController,
    grupId: String,
    grupNom: String,
    menuNom: String,
    collectionName: String,
    setName: String,
    authViewModel: AuthViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CustomDrawer(grupNom, menuNom, navController, authViewModel, grupId)
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    CustomTopBar(
                        grupNom = grupNom,
                        menuNom = menuNom,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                    BreadcrumbBar(listOf(grupNom, menuNom, collectionName, setName, item.nom))
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.nom, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        if (item.marca.isNotBlank()) Text("Marca: ${item.marca}")
                        if (item.model.isNotBlank()) Text("Model: ${item.model}")
                        if (item.descripcio.isNotBlank()) Text("Descripció: ${item.descripcio}")
                        Text("Quantitat: ${item.quantitat}")
                    }
                }
            }
        }
    }
}