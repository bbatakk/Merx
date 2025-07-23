package com.rokobanana.merx.ui.seleccionarGrup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.auth.FirebaseAuth

@Composable
fun TriarGrupScreen(navController: NavController, onGrupSelected: (String) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var grups by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // Pair<id, nom>
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("usuaris").document(userId).get()
                .addOnSuccessListener { doc ->
                    val grupIds = (doc.get("grups") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    if (grupIds.isEmpty()) {
                        loading = false
                        error = "No tens cap grup associat"
                    } else {
                        db.collection("grups").whereIn(FieldPath.documentId(), grupIds)
                            .get()
                            .addOnSuccessListener { result ->
                                grups = result.documents.map { it.id to (it.getString("nom") ?: "Sense nom") }
                                loading = false
                            }
                            .addOnFailureListener {
                                loading = false
                                error = "Error carregant grups"
                            }
                    }
                }
                .addOnFailureListener {
                    loading = false
                    error = "Error accedint a l'usuari"
                }
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(error!!)
        }
    } else {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
        ) {
            Text("Escull un grup", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            grups.forEach { (id, nom) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onGrupSelected(id) }
                ) {
                    Text(
                        text = nom,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}