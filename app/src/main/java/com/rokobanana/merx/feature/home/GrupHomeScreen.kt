package com.rokobanana.merx.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import com.rokobanana.merx.feature.components.CustomDrawer
import com.rokobanana.merx.feature.components.CustomTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrupHomeScreen(
    grupNom: String,
    menuNom: String = "Llista de Productes",
    navController: NavController,
    authViewModel: AuthViewModel,
    grupId: String
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
                CustomTopBar(
                    grupNom = grupNom,
                    menuNom = menuNom,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    onClick = { navController.navigate("llistaProductes/$grupId") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "GESTIÓ D'ESTOC",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                Card(
                    onClick = { navController.navigate("colleccionsMaterial/$grupId") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "MATERIAL",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                // --- NOU BOTÓ CARREGAR MATERIAL ---
                Card(
                    onClick = { navController.navigate("carregarMaterial/$grupId") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "CARREGAR MATERIAL",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}