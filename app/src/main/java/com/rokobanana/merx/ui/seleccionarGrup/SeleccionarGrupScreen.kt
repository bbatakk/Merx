package com.rokobanana.merx.ui.seleccionarGrup

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.ui.autenticacio.AuthViewModel
import com.rokobanana.merx.ui.autenticacio.AuthViewModelFactory

@Composable
fun SeleccionarGrupScreen(navController: NavController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext as Application))
    val user by authViewModel.userState.collectAsState()

    var grupNom by remember { mutableStateOf("") }
    var grupClau by remember { mutableStateOf("") }
    var joinError by remember { mutableStateOf<String?>(null) }
    var loadingJoin by remember { mutableStateOf(false) }

    var nouGrupNom by remember { mutableStateOf("") }
    var novaClau by remember { mutableStateOf("") }
    var createError by remember { mutableStateOf<String?>(null) }
    var loadingCreate by remember { mutableStateOf(false) }

    fun actualitzaUsuariAmbGrup(grupId: String, onFinished: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return onFinished(false)
        val db = FirebaseFirestore.getInstance()
        val usuariRef = db.collection("usuaris").document(uid)
        db.runTransaction { transaction ->
            val usuariSnap = transaction.get(usuariRef)
            val grups = (usuariSnap.get("grups") as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
            if (!grups.contains(grupId)) grups.add(grupId)
            transaction.set(usuariRef, mapOf("grups" to grups, "grupActual" to grupId), com.google.firebase.firestore.SetOptions.merge())
        }.addOnSuccessListener { onFinished(true) }
            .addOnFailureListener { onFinished(false) }
    }

    fun unirALGrup(grupId: String) {
        actualitzaUsuariAmbGrup(grupId) { ok ->
            if (ok) {
                navController.navigate("llista/$grupId") {
                    popUpTo("seleccio") { inclusive = true }
                }
            } else {
                joinError = "Error afegint el grup a l'usuari"
            }
        }
    }

    fun handleJoinGroup() {
        loadingJoin = true; joinError = null
        val db = FirebaseFirestore.getInstance()
        db.collection("grups")
            .whereEqualTo("nom", grupNom.trim())
            .get()
            .addOnSuccessListener { result ->
                val grup = result.documents.firstOrNull()
                if (grup != null && grup.getString("clauAcces") == grupClau.trim()) {
                    unirALGrup(grup.id)
                } else {
                    loadingJoin = false
                    joinError = "Nom o clau incorrectes"
                }
            }
            .addOnFailureListener {
                loadingJoin = false
                joinError = "Error accedint a Firestore"
            }
    }

    fun handleCreateGroup() {
        loadingCreate = true; createError = null
        val db = FirebaseFirestore.getInstance()
        val nouNom = nouGrupNom.trim()
        val novaClauFinal = novaClau.trim()
        db.collection("grups").whereEqualTo("nom", nouNom).get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    db.collection("grups").add(
                        mapOf("nom" to nouNom, "clauAcces" to novaClauFinal)
                    ).addOnSuccessListener { ref ->
                        unirALGrup(ref.id)
                    }.addOnFailureListener {
                        loadingCreate = false
                        createError = "Error creant el grup"
                    }
                } else {
                    loadingCreate = false
                    createError = "Ja existeix un grup amb aquest nom"
                }
            }
            .addOnFailureListener {
                loadingCreate = false
                createError = "Error comprovant existència del grup"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Unir-se a un grup existent", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = grupNom,
            onValueChange = { grupNom = it },
            label = { Text("Nom del grup") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = grupClau,
            onValueChange = { grupClau = it },
            label = { Text("Clau d'accés") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { handleJoinGroup() },
            enabled = grupNom.isNotBlank() && grupClau.isNotBlank() && !loadingJoin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar al grup")
        }
        if (loadingJoin) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }
        joinError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(32.dp))

        Text("Crear un grup nou", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = nouGrupNom,
            onValueChange = { nouGrupNom = it },
            label = { Text("Nom del nou grup") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = novaClau,
            onValueChange = { novaClau = it },
            label = { Text("Clau d'accés pel grup") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { handleCreateGroup() },
            enabled = nouGrupNom.isNotBlank() && novaClau.isNotBlank() && !loadingCreate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear i unir-se")
        }
        if (loadingCreate) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator()
        }
        createError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}