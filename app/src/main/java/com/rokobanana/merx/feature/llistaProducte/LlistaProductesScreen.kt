package com.rokobanana.merx.feature.llistaProducte

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModel
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModelFactory
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import com.rokobanana.merx.feature.autenticacio.AuthViewModelFactory
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlistaProductesScreen(
    navController: NavController,
    grupId: String,
    viewModel: ProductesViewModel = viewModel(factory = ProductesViewModelFactory(grupId))
) {
    BackHandler {
        // No fem res: l'usuari no pot tornar enrere
    }
    val productes by viewModel.productes.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext as Application))
    val loading by viewModel.loading.collectAsState()

    var grupNom by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(grupId) {
        val db = FirebaseFirestore.getInstance()
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
            FloatingActionButton(onClick = {
                navController.navigate("nou/$grupId")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Afegir")
            }
        }
    ) { paddingValues ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                                    Text(
                                        text = "Estoc: $estocTotal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (estocTotal == 0) Color(0xFFFFCDD2) else Color.White.copy(
                                            alpha = 0.7f
                                        )
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