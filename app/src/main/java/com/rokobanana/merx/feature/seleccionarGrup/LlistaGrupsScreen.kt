package com.rokobanana.merx.feature.seleccionarGrup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlistaGrupsScreen(navController: NavController, userId: String) {
    val db = FirebaseFirestore.getInstance()
    var grups by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var grupAVincular by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("usuaris").document(userId).get()
                .addOnSuccessListener { doc ->
                    val grupIds = (doc.get("grups") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    if (grupIds.isNotEmpty()) {
                        db.collection("grups").whereIn(FieldPath.documentId(), grupIds).get()
                            .addOnSuccessListener { snapshot ->
                                grups = snapshot.documents.map { docu ->
                                    docu.id to (docu.getString("nom") ?: docu.id)
                                }
                            }
                    } else {
                        grups = emptyList()
                    }
                }
        } else {
            grups = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Els meus grups") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Enrere")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Llista de grups",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (grups.isEmpty()) {
                    Text("No estàs vinculat a cap grup.")
                } else {
                    grups.forEach { (grupId, nomGrup) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    nomGrup,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    grupAVincular = grupId to nomGrup
                                    showDialog = true
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sortir del grup")
                                }
                            }
                        }
                    }
                }
            }
            // AlertDialog de confirmació
            if (showDialog && grupAVincular != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmar desvinculació") },
                    text = { Text("Segur que vols sortir del grup \"${grupAVincular!!.second}\"?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            val grupId = grupAVincular!!.first
                            db.collection("usuaris").document(userId)
                                .update("grups", FieldValue.arrayRemove(grupId))
                                .addOnSuccessListener {
                                    db.collection("grups").document(grupId)
                                        .collection("membres").document(userId).delete()
                                    grups = grups.filterNot { it.first == grupId }
                                    if (grups.isEmpty()) {
                                        navController.navigate("menuGrups") {
                                            popUpTo("menuGrups") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                        }) {
                            Text("Sí, sortir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel·la")
                        }
                    }
                )
            }
        }
    }
}