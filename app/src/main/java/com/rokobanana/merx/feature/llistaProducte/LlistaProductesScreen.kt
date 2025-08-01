package com.rokobanana.merx.feature.llistaProducte

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModel
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlistaProductesScreen(
    navController: NavController,
    grupId: String,
    productesViewModel: ProductesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    BackHandler { /* No fem res */ }

    val productes by productesViewModel.productes.collectAsState()
    val loading by productesViewModel.loading.collectAsState()
    val error by productesViewModel.error.collectAsState()
    var grupNom by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    val isAdmin by productesViewModel.isAdmin.collectAsState()
    val usuariId = authViewModel.userState.collectAsState().value?.id

    // Carregar productes i rol (sempre via ViewModel)
    LaunchedEffect(grupId) {
        productesViewModel.carregarProductes(grupId)
    }
    LaunchedEffect(grupId, usuariId) {
        productesViewModel.carregarRol(grupId, usuariId)
    }

    // Carregar el nom del grup
    LaunchedEffect(grupId) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val doc = db.collection("grups").document(grupId).get().await()
        grupNom = doc.getString("nom") ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "MERX",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (grupNom.isNotEmpty()) {
                            Text(
                                text = "  |  $grupNom",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Perfil") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("perfil")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Grups") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("menuGrups") {
                                        popUpTo("llista/$grupId") { inclusive = true }
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Tancar sessió") },
                                onClick = {
                                    menuExpanded = false
                                    authViewModel.signOut()
                                    navController.navigate("login") {
                                        popUpTo("seleccio") { inclusive = true }
                                    }
                                },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Tancar sessió") }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                Box {
                    FloatingActionButton(onClick = { fabMenuExpanded = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Afegir")
                    }
                    DropdownMenu(
                        expanded = fabMenuExpanded,
                        onDismissRequest = { fabMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Afegir producte") },
                            onClick = {
                                fabMenuExpanded = false
                                navController.navigate("nou/$grupId")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Gestió col·leccions de material") },
                            onClick = {
                                fabMenuExpanded = false
                                navController.navigate("colleccionsMaterial/$grupId")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Afegir set de material") },
                            onClick = {
                                fabMenuExpanded = false
                                navController.navigate("nouSet/$grupId")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Veure llista de sets") },
                            onClick = {
                                fabMenuExpanded = false
                                navController.navigate("llistaSets/$grupId")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Carregar material a la furgoneta") },
                            onClick = {
                                fabMenuExpanded = false
                                navController.navigate("carregarSets/$grupId")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Properament: Afegir esdeveniment") },
                            onClick = { /* acció futura */ },
                            enabled = false
                        )
                    }
                }
            }
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
                                modifier = Modifier.clickable { navController.navigate("detall/$grupId/${producte.id}") }
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