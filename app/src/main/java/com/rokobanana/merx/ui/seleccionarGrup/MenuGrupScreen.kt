package com.rokobanana.merx.ui.seleccionarGrup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun MenuGrupsScreen(
    navController: NavController,
    onBackPressed: (() -> Unit)? = null
) {
    val user = FirebaseAuth.getInstance().currentUser
    var grups by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var nouNomGrup by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var grupNoExisteix by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var groupIdToJoin by remember { mutableStateOf<String?>(null) }
    var inputKey by remember { mutableStateOf("") }
    var errorKey by remember { mutableStateOf<String?>(null) }
    var novaClau by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val usuariDoc = db.collection("usuaris").document(user.uid).get().await()
            val grupIds = (usuariDoc.get("grups") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val grupsList = mutableListOf<Pair<String, String>>()
            for (grupId in grupIds) {
                val grupDoc = db.collection("grups").document(grupId).get().await()
                val nom = grupDoc.getString("nom") ?: grupId
                grupsList.add(grupId to nom)
            }
            grups = grupsList
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Els teus grups") },
                navigationIcon = {
                    IconButton(onClick = {
                        onBackPressed?.invoke() ?: navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tornar enrere"
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                grups.forEach { (grupId, nomGrup) ->
                    Button(
                        onClick = { navController.navigate("llista/$grupId") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(nomGrup)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Entra a un grup existent o crea'n un de nou:",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nouNomGrup,
                    onValueChange = { nouNomGrup = it },
                    label = { Text("Nom del grup") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        errorText = null
                        grupNoExisteix = false
                        showDialog = false

                        val db = FirebaseFirestore.getInstance()
                        db.collection("grups")
                            .whereEqualTo("nom", nouNomGrup.trim())
                            .get()
                            .addOnSuccessListener { result ->
                                if (!result.isEmpty) {
                                    groupIdToJoin = result.documents.first().id
                                    showKeyDialog = true
                                } else {
                                    grupNoExisteix = true
                                    showDialog = true
                                }
                            }
                            .addOnFailureListener {
                                errorText = "Error consultant grups: ${it.localizedMessage}"
                            }
                    },
                    enabled = nouNomGrup.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nou grup")
                }

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                // Diàleg per introduir la clau del grup existent
                if (showKeyDialog && groupIdToJoin != null) {
                    AlertDialog(
                        onDismissRequest = { showKeyDialog = false; inputKey = ""; errorKey = null },
                        title = { Text("Introdueix la clau del grup") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = inputKey,
                                    onValueChange = { inputKey = it },
                                    label = { Text("Clau") }
                                )
                                if (errorKey != null) {
                                    Text(errorKey!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("grups").document(groupIdToJoin!!).get()
                                    .addOnSuccessListener { grupDoc ->
                                        val keyNeeded = grupDoc.getString("clau") ?: ""
                                        if (inputKey == keyNeeded) {
                                            val userId = user?.uid
                                            if (userId != null) {
                                                val usuariDoc = db.collection("usuaris").document(userId)
                                                usuariDoc.get().addOnSuccessListener { document ->
                                                    if (!document.contains("grups")) {
                                                        usuariDoc.set(hashMapOf("grups" to listOf<String>()), com.google.firebase.firestore.SetOptions.merge())
                                                            .addOnSuccessListener {
                                                                usuariDoc.update("grups", FieldValue.arrayUnion(groupIdToJoin!!))
                                                                    .addOnSuccessListener {
                                                                        showKeyDialog = false
                                                                        navController.navigate("llista/$groupIdToJoin")
                                                                    }
                                                                    .addOnFailureListener { errorKey = "Error afegint-te al grup: ${it.localizedMessage}" }
                                                            }
                                                            .addOnFailureListener { errorKey = "Error inicialitzant l'usuari: ${it.localizedMessage}" }
                                                    } else {
                                                        usuariDoc.update("grups", FieldValue.arrayUnion(groupIdToJoin!!))
                                                            .addOnSuccessListener {
                                                                showKeyDialog = false
                                                                navController.navigate("llista/$groupIdToJoin")
                                                            }
                                                            .addOnFailureListener { errorKey = "Error afegint-te al grup: ${it.localizedMessage}" }
                                                    }
                                                }
                                            }
                                        } else {
                                            errorKey = "Clau incorrecta"
                                        }
                                    }
                                    .addOnFailureListener {
                                        errorKey = "Error consultant la clau: ${it.localizedMessage}"
                                    }
                            }) { Text("Entrar") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showKeyDialog = false; inputKey = ""; errorKey = null }) {
                                Text("Cancel·lar")
                            }
                        }
                    )
                }

                // Diàleg per confirmar creació de nou grup
                if (showDialog && grupNoExisteix) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Grup no existeix") },
                        text = {
                            Column {
                                Text("Aquest grup no existeix. Vols crear-lo?")
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = novaClau,
                                    onValueChange = { novaClau = it },
                                    label = { Text("Clau pel nou grup") }
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                val db = FirebaseFirestore.getInstance()
                                val nouGrup = hashMapOf(
                                    "nom" to nouNomGrup.trim(),
                                    "clau" to novaClau
                                )
                                val userId = user?.uid
                                val grupRef = db.collection("grups").document()
                                grupRef.set(nouGrup)
                                    .addOnSuccessListener {
                                        if (userId != null) {
                                            db.collection("usuaris").document(userId)
                                                .update("grups", FieldValue.arrayUnion(grupRef.id))
                                                .addOnSuccessListener {
                                                    navController.navigate("llista/${grupRef.id}")
                                                }
                                        }
                                    }
                                showDialog = false
                            }) {
                                Text("Sí, crear")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showDialog = false }) {
                                Text("Cancel·lar")
                            }
                        }
                    )
                }
            }
        }
    }
}