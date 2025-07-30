package com.rokobanana.merx.feature.editarProducte

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModel
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModelFactory
import kotlinx.coroutines.launch
import com.rokobanana.merx.domain.model.RolMembre
import com.rokobanana.merx.feature.membres.MembresRepository
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("AutoboxingStateCreation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallProducteScreen(
    grupId: String,
    producteId: String,
    navController: NavController,
    viewModel: ProductesViewModel = viewModel(factory = ProductesViewModelFactory(grupId))
) {
    // ÚNIC scope per accions UI!
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var esAdmin by remember { mutableStateOf(false) }
    var rolCarregant by remember { mutableStateOf(true) }
    val usuariId = FirebaseAuth.getInstance().currentUser?.uid

    // Estat global de loading i error del ViewModel
    val loadingGlobal by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Estat del producte concret
    val producte by viewModel.getProducte(producteId).collectAsState(initial = null)

    // Altres estats locals
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showUnsavedDialog = remember { mutableStateOf(false) }
    val backgroundColor = MaterialTheme.colorScheme.background
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }
    val estocInicialPerTalla = remember { mutableStateMapOf<String, Int>() }
    val estocEditat = remember { mutableStateMapOf<String, Int>() }
    var isSaving by remember { mutableStateOf(false) }
    var valorPreuText by remember { mutableStateOf("") }
    var preuEditat by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(grupId, usuariId) {
        if (usuariId != null) {
            rolCarregant = true
            val membresRepo = MembresRepository()
            val membres = membresRepo.membresDeGrup(grupId)
            val membre = membres.find { it.usuariId == usuariId }
            esAdmin = membre?.rol == RolMembre.ADMIN
            rolCarregant = false
        }
    }

    // Inicialització d'estoc i preu cada cop que el producte canvia
    LaunchedEffect(producte?.id) {
        val prod = producte ?: return@LaunchedEffect
        estocInicialPerTalla.clear()
        estocEditat.clear()
        prod.estocPerTalla.forEach { (talla, quantitat) ->
            estocInicialPerTalla[talla] = quantitat
            estocEditat[talla] = quantitat
        }
        valorPreuText = "%.2f".format(prod.preu)
        preuEditat = prod.preu
    }

    // Feedback d'error global (Firestore, update, delete) via Snackbar (NO hi ha scope.launch aquí!)
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Detecta si hi ha canvis sense guardar
    val hasUnsavedChanges = estocInicialPerTalla.any { (talla, quantitat) ->
        estocEditat[talla] != quantitat
    } || preuEditat != (producte?.preu ?: 0.0)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Editar Estoc ${producte?.tipus ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            showUnsavedDialog.value = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
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
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        isSaving = true
                        estocEditat.forEach { (talla, quantitat) ->
                            viewModel.updateEstoc(producteId, talla, quantitat)
                        }
                        viewModel.updatePreu(producteId, preuEditat)
                        scope.launch {
                            estocInicialPerTalla.clear()
                            estocInicialPerTalla.putAll(estocEditat)
                            isSaving = false
                            snackbarHostState.showSnackbar("Estoc actualitzat")
                        }
                    },
                    enabled = hasUnsavedChanges && !isSaving && !loadingGlobal,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar")
                    }
                }

                if (esAdmin && !rolCarregant) {
                    Button(
                        onClick = { showDeleteDialog.value = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        enabled = !isSaving && !loadingGlobal
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (loadingGlobal || producte == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            producte?.let { prod ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                ) {
                    // Imatge destacada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        AsyncImage(
                            model = prod.imageUrl,
                            contentDescription = prod.nom,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showDialog = true }
                        )
                        if (showDialog) {
                            Dialog(onDismissRequest = { showDialog = false }) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    AsyncImage(
                                        model = prod.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showDialog = false }
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = prod.nom,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = prod.tipus,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Contingut editable
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (prod.usaTalles) {
                            val ordreTalles = listOf("XS", "S", "M", "L", "XL", "XXL")
                            ordreTalles.forEach { talla ->
                                val quantitat by remember {
                                    derivedStateOf { estocEditat[talla] ?: prod.estocPerTalla[talla] ?: 0 }
                                }
                                var valorText by remember { mutableStateOf(quantitat.toString()) }
                                LaunchedEffect(quantitat) { valorText = quantitat.toString() }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(talla, modifier = Modifier.width(40.dp))

                                    Spacer(Modifier.weight(1f))

                                    IconButton(
                                        onClick = {
                                            val nouValor = (valorText.toIntOrNull() ?: 0) - 1
                                            valorText = nouValor.coerceAtLeast(0).toString()
                                            estocEditat[talla] = nouValor.coerceAtLeast(0)
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        },
                                        modifier = Modifier.size(32.dp),
                                        enabled = !isSaving
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Remove,
                                            contentDescription = "Reduir estoc",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    TextField(
                                        value = valorText,
                                        onValueChange = { newText: String ->
                                            val filtered = newText.filter { it.isDigit() }
                                            valorText = filtered
                                            estocEditat[talla] = filtered.toIntOrNull() ?: 0
                                        },
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(56.dp),
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            textAlign = TextAlign.Center
                                        ),
                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            cursorColor = MaterialTheme.colorScheme.primary
                                        ),
                                        enabled = !isSaving
                                    )

                                    IconButton(
                                        onClick = {
                                            val nouValor = (valorText.toIntOrNull() ?: 0) + 1
                                            valorText = nouValor.toString()
                                            estocEditat[talla] = nouValor
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        },
                                        modifier = Modifier.size(32.dp),
                                        enabled = !isSaving
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Augmentar estoc",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        } else {
                            val quantitatGeneral by remember {
                                derivedStateOf { estocEditat["general"] ?: prod.estocPerTalla["general"] ?: 0 }
                            }
                            var valorGeneralText by remember { mutableStateOf(quantitatGeneral.toString()) }
                            LaunchedEffect(quantitatGeneral) { valorGeneralText = quantitatGeneral.toString() }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    "Estoc",
                                    modifier = Modifier.width(60.dp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Spacer(Modifier.weight(1f))

                                IconButton(
                                    onClick = {
                                        val nouValor = (valorGeneralText.toIntOrNull() ?: 0) - 1
                                        valorGeneralText = nouValor.coerceAtLeast(0).toString()
                                        estocEditat["general"] = nouValor.coerceAtLeast(0)
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    },
                                    modifier = Modifier.size(32.dp),
                                    enabled = !isSaving
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Remove,
                                        contentDescription = "Reduir estoc",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                TextField(
                                    value = valorGeneralText,
                                    onValueChange = { newText: String ->
                                        val filtered = newText.filter { it.isDigit() }
                                        valorGeneralText = filtered
                                        estocEditat["general"] = filtered.toIntOrNull() ?: 0
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(56.dp),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    enabled = !isSaving
                                )

                                IconButton(
                                    onClick = {
                                        val nouValor = (valorGeneralText.toIntOrNull() ?: 0) + 1
                                        valorGeneralText = nouValor.toString()
                                        estocEditat["general"] = nouValor
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    },
                                    modifier = Modifier.size(32.dp),
                                    enabled = !isSaving
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Augmentar estoc",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.padding(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    "Preu",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .width(70.dp)
                                        .padding(end = 8.dp)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = valorPreuText,
                                    onValueChange = { newText: String ->
                                        val filtered = newText.filter { it.isDigit() || it == '.' }
                                        valorPreuText = filtered
                                        preuEditat = filtered.toDoubleOrNull() ?: 0.0
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(56.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(14.dp)
                                        ),
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        textAlign = TextAlign.End
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(14.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Money,
                                            contentDescription = "Euro",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    enabled = !isSaving
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diàleg de confirmació d'eliminació
        if (showDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text("Eliminar Producte") },
                text = { Text("Segur que vols eliminar aquest producte? Aquesta acció no es pot desfer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.eliminarProducte(producteId)
                            showDeleteDialog.value = false
                            navController.popBackStack()
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog.value = false }
                    ) {
                        Text("Cancel·lar")
                    }
                }
            )
        }

        // Diàleg de canvis sense guardar
        if (showUnsavedDialog.value) {
            AlertDialog(
                onDismissRequest = { showUnsavedDialog.value = false },
                title = { Text("Canvis sense guardar") },
                text = { Text("Tens canvis sense guardar. Vols sortir igualment?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showUnsavedDialog.value = false
                            navController.popBackStack()
                        }
                    ) {
                        Text("Sí, sortir")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showUnsavedDialog.value = false }
                    ) {
                        Text("Cancel·lar")
                    }
                }
            )
        }
    }
}