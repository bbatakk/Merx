package com.rokobanana.merx.feature.afegirProducte.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rokobanana.merx.domain.model.Producte
import com.rokobanana.merx.core.utils.pujarImatgeAStorage
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModel
import com.rokobanana.merx.core.GrupGlobalViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokobanana.merx.domain.model.RolMembre
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AfegirProducteScreen(
    navController: NavController,
    viewModel: ProductesViewModel = hiltViewModel()
) {
    // Obtenim el grupId i el rol del ViewModel global
    val grupGlobalViewModel: GrupGlobalViewModel = hiltViewModel()
    val grupId by grupGlobalViewModel.grupId.collectAsState()
    val userRol by grupGlobalViewModel.userRol.collectAsState() // Si vols controlar accions per rol

    var nom by remember { mutableStateOf("") }
    var tipus by remember { mutableStateOf("") }
    var usaTalles by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }
    var preu by remember { mutableDoubleStateOf(0.0) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    loading = true
                    val url = pujarImatgeAStorage(uri)
                    imageUrl = url
                    error = null
                } catch (e: Exception) {
                    error = "Error pujant la imatge: ${e.localizedMessage}"
                    snackbarHostState.showSnackbar(error ?: "Error pujant la imatge")
                } finally {
                    loading = false
                }
            }
        }
    }

    // Snackbar per errors d'afegir producte
    LaunchedEffect(error) {
        error?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    // Si el grupId no està disponible, mostra loading o error
    if (grupId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Afegir Producte") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Enrere"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Opcional: només deixar afegir producte si és admin
                val potAfegir = userRol == RolMembre.ADMIN || userRol == null // Adaptar segons el teu enum/valor
                Button(
                    onClick = {
                        loading = true
                        val estoc = if (usaTalles) {
                            mapOf(
                                "XS" to 0,
                                "S" to 0,
                                "M" to 0,
                                "L" to 0,
                                "XL" to 0,
                                "XXL" to 0
                            )
                        } else {
                            mapOf("general" to 0)
                        }
                        val nou = Producte(
                            nom = nom,
                            tipus = tipus,
                            usaTalles = usaTalles,
                            imageUrl = imageUrl,
                            estocPerTalla = estoc,
                            preu = preu
                        )
                        coroutineScope.launch {
                            try {
                                // grupId!! ja és no nul aquí
                                viewModel.afegirProducte(nou, grupId!!)
                                loading = false
                                navController.popBackStack()
                            } catch (e: Exception) {
                                error = "Error afegint producte: ${e.localizedMessage}"
                                loading = false
                            }
                        }
                    },
                    enabled = potAfegir && nom.isNotBlank() && tipus.isNotBlank() && imageUrl.isNotEmpty() && !loading,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Afegir producte")
                    }
                }
                if (!potAfegir) {
                    Text(
                        text = "Només els administradors poden afegir productes.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                TextField(
                    value = nom,
                    onValueChange = { nom = it; error = null },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                )

                Spacer(Modifier.height(8.dp))

                TextField(
                    value = tipus,
                    onValueChange = { tipus = it; error = null },
                    label = { Text("Tipus") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                )

                Spacer(Modifier.height(8.dp))

                TextField(
                    value = if (preu == 0.0) "" else preu.toString(),
                    onValueChange = { text ->
                        val filtered = text.filter { it.isDigit() || it == '.' }
                        preu = filtered.toDoubleOrNull() ?: 0.0
                        error = null
                    },
                    label = { Text("Preu (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !loading
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = usaTalles,
                        onCheckedChange = { usaTalles = it },
                        enabled = !loading
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Utilitza talles")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    enabled = !loading
                ) {
                    Text("Seleccionar imatge")
                }

                Spacer(Modifier.height(16.dp))

                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(500.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    )
}