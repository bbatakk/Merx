package com.rokobanana.merx.feature.llistaProducte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModel
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rokobanana.merx.feature.components.CustomTopBar
import com.rokobanana.merx.feature.components.CustomDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlistaProductesScreen(
    navController: NavController,
    grupId: String,
    grupNom: String,
    menuNom: String,
    authViewModel: AuthViewModel,
    productesViewModel: ProductesViewModel = hiltViewModel()
) {
    val productes by productesViewModel.productes.collectAsState()
    val loading by productesViewModel.loading.collectAsState()
    val error by productesViewModel.error.collectAsState()
    val isAdmin by productesViewModel.isAdmin.collectAsState()
    val usuari by authViewModel.userState.collectAsState()
    val usuariId = usuari?.id

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    println("DEBUG usuariId a la pantalla: $usuariId")
    println("DEBUG isAdmin a la pantalla: $isAdmin")

    // Mostra loader si encara estem esperant l'usuariId després de login
    /*if (usuariId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        // No fem res més fins que tinguem usuariId
        return
    }*/

    // Només carreguem rol quan tenim grupId i usuariId
    LaunchedEffect(grupId, usuariId) {
        if (grupId.isNotEmpty() && usuariId != null) {
            productesViewModel.carregarRol(grupId, usuariId)
        } else {
            productesViewModel.clearRol()
        }
    }

    LaunchedEffect(grupId) {
        if (grupId.isNotEmpty()) {
            productesViewModel.carregarProductes(grupId)
        }
    }

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
                    onMenuClick = { scope.launch { drawerState.open() } },
                    actions = {
                        if (isAdmin) {
                            IconButton(
                                onClick = { navController.navigate("nouProducte/$grupId") }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Afegir producte")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { productesViewModel.clearError() }) {
                                Text("Tornar a intentar")
                            }
                        }
                    }
                }
                productes.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hi ha productes a aquest grup.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(productes) { producte ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectangleShape
                            ) {
                                Column(
                                    modifier = Modifier.clickable {
                                        navController.navigate("detall/$grupId/${producte.id}")
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    ) {
                                        AsyncImage(
                                            model = producte.imageUrl,
                                            contentDescription = producte.nom,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.4f))
                                        )
                                        val estocTotal = if (producte.usaTalles) {
                                            producte.estocPerTalla.values.sum()
                                        } else {
                                            producte.estocPerTalla["general"] ?: 0
                                        }
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = producte.nom,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = producte.tipus,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = "Estoc: $estocTotal",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (estocTotal == 0) Color(0xFFFFCDD2) else Color.White.copy(alpha = 0.7f),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "Preu: %.2f €".format(producte.preu),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White,
                                                    modifier = Modifier
                                                        .padding(start = 8.dp)
                                                        .align(Alignment.CenterVertically)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}