package com.rokobanana.merx.feature.afegirProducte.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.domain.model.Producte
import com.rokobanana.merx.core.utils.pujarImatgeAStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModel
import com.rokobanana.merx.feature.afegirProducte.ProductesViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AfegirProducteScreen(
    navController: NavController,
    grupId: String
) {
    val context = LocalContext.current

    val viewModel: ProductesViewModel = viewModel(
        factory = ProductesViewModelFactory(grupId = grupId)
    )

    var nom by remember { mutableStateOf("") }
    var tipus by remember { mutableStateOf("") }
    var usaTalles by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading = true
                    val url = pujarImatgeAStorage(uri)
                    withContext(Dispatchers.Main) {
                        imageUrl = url
                        error = null
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        error = "Error pujant la imatge: ${e.localizedMessage}"
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        loading = false
                    }
                }
            }
        }
    }

    Scaffold(
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
                Button(
                    onClick = {
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
                            estocPerTalla = estoc
                        )

                        viewModel.afegirProducte(nou)
                        navController.popBackStack()
                    },
                    enabled = nom.isNotBlank() && tipus.isNotBlank(),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Afegir producte")
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
                    onValueChange = { nom = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                TextField(
                    value = tipus,
                    onValueChange = { tipus = it },
                    label = { Text("Tipus") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = usaTalles,
                        onCheckedChange = { usaTalles = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Utilitza talles")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
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

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    )
}
