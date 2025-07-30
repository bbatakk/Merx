package com.rokobanana.merx.feature.seleccionarGrup

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
import com.rokobanana.merx.feature.autenticacio.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.rokobanana.merx.domain.model.RolMembre

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuGrupsScreen(
    navController: NavController
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
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext as Application))
    var grupADesvincular by remember { mutableStateOf<Pair<String, String>?>(null) }
    var grupAEditar by remember { mutableStateOf<Pair<String, String>?>(null) }
    var editNomGrup by remember { mutableStateOf("") }
    var editClauGrup by remember { mutableStateOf("") }

    BackHandler {
        // No fem res: l'usuari no pot tornar enrere
    }

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
                title = { Text("Menú Grups") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("seleccio") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Tancar sessió")
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
                    var expandedMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { navController.navigate("llista/$grupId") },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = nomGrup,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Box {
                                IconButton(onClick = { expandedMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opcions del grup")
                                }
                                DropdownMenu(
                                    expanded = expandedMenu,
                                    onDismissRequest = { expandedMenu = false }
                                ) {
                                   DropdownMenuItem(
                                        text = { Text("Editar grup") },
                                        onClick = {
                                            expandedMenu = false
                                            grupAEditar = grupId to nomGrup
                                            // Mostrarem el diàleg d'edició després
                                        },
                                       leadingIcon = {
                                           Icon(
                                               imageVector = Icons.Default.Edit,
                                               contentDescription = "Editar grup"
                                           )
                                       }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar grup") },
                                        onClick = {
                                            expandedMenu = false
                                            grupADesvincular = grupId to nomGrup
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Eliminar grup"
                                            )
                                        }
                                    )
                                }
                            }
                        }
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
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nou grup")
                }

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                if (showKeyDialog && groupIdToJoin != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showKeyDialog = false
                            inputKey = ""
                            groupIdToJoin = null
                            errorKey = null
                        },
                        title = { Text("Introdueix la clau") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = inputKey,
                                    onValueChange = { inputKey = it },
                                    label = { Text("Clau del grup") }
                                )
                                if (errorKey != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(errorKey ?: "", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("grups").document(groupIdToJoin!!).get()
                                    .addOnSuccessListener { grupDoc ->
                                        val clau = grupDoc.getString("clau") ?: ""
                                        if (inputKey == clau) {
                                            val userId = user?.uid
                                            if (userId != null) {
                                                db.collection("usuaris").document(userId)
                                                    .update("grups", FieldValue.arrayUnion(groupIdToJoin))
                                                    .addOnSuccessListener {
                                                        // Afegeix el membre a la col·lecció membres
                                                        val membresRepo = com.rokobanana.merx.feature.membres.MembresRepository()
                                                        val membre = com.rokobanana.merx.domain.model.Membre(
                                                            usuariId = userId,
                                                            grupId = groupIdToJoin!!,
                                                            rol = RolMembre.MEMBRE
                                                        )
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            membresRepo.afegirMembre(membre)
                                                            withContext(Dispatchers.Main) {
                                                                navController.navigate("llista/${groupIdToJoin}") {
                                                                    popUpTo("menuGrups") { inclusive = true }
                                                                }
                                                            }
                                                        }
                                                    }
                                            }
                                        } else {
                                            errorKey = "Clau incorrecta."
                                        }
                                    }
                            }) { Text("Entrar") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = {
                                showKeyDialog = false
                                inputKey = ""
                                groupIdToJoin = null
                                errorKey = null
                            }) { Text("Cancel·lar") }
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
                                    label = { Text("Clau pel nou grup") },
                                    isError = novaClau.isBlank()
                                )
                                if (novaClau.isBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Has d'introduir una clau per crear el grup.",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (novaClau.isBlank()) return@Button  // No permetis continuar sense clau!
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
                                                        val membresRepo = com.rokobanana.merx.feature.membres.MembresRepository()
                                                        val membre = com.rokobanana.merx.domain.model.Membre(
                                                            usuariId = userId,
                                                            grupId = grupRef.id,
                                                            rol = RolMembre.ADMIN
                                                        )
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            membresRepo.afegirMembre(membre)
                                                            withContext(Dispatchers.Main) {
                                                                navController.navigate("llista/${grupRef.id}") {
                                                                    popUpTo("menuGrups") { inclusive = true }
                                                                }
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                    showDialog = false
                                },
                                enabled = novaClau.isNotBlank() // Només permet el botó si hi ha clau!
                            ) { Text("Sí, crear") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showDialog = false }) {
                                Text("Cancel·lar")
                            }
                        }
                    )
                }

                // Diàleg per confirmar desvinculació d'un grup
                if (grupADesvincular != null && user != null) {
                    AlertDialog(
                        onDismissRequest = { grupADesvincular = null },
                        title = { Text("Desvincular-se del grup") },
                        text = { Text("Segur que vols desvincular-te del grup \"${grupADesvincular!!.second}\"?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val db = FirebaseFirestore.getInstance()
                                    val userId = user.uid
                                    val grupId = grupADesvincular!!.first

                                    // 1. Elimina el grup de la llista de l'usuari
                                    db.collection("usuaris").document(userId)
                                        .update("grups", FieldValue.arrayRemove(grupId))

                                    // 2. Elimina la vinculació a la col·lecció membres
                                    db.collection("membres")
                                        .whereEqualTo("usuariId", userId)
                                        .whereEqualTo("grupId", grupId)
                                        .get()
                                        .addOnSuccessListener { snapshot ->
                                            val batch = db.batch()
                                            for (doc in snapshot.documents) {
                                                batch.delete(doc.reference)
                                            }
                                            batch.commit().addOnSuccessListener {
                                                grupADesvincular = null
                                                // Refresca la pantalla
                                                loading = true
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val usuariDoc = db.collection("usuaris").document(userId).get().await()
                                                    val grupIds = (usuariDoc.get("grups") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                                    val grupsList = mutableListOf<Pair<String, String>>()
                                                    for (gid in grupIds) {
                                                        val grupDoc = db.collection("grups").document(gid).get().await()
                                                        val nom = grupDoc.getString("nom") ?: gid
                                                        grupsList.add(gid to nom)
                                                    }
                                                    grups = grupsList
                                                    loading = false
                                                }
                                            }
                                        }
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                            ) { Text("Sí, desvincular") }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { grupADesvincular = null },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Cancel·lar")}
                        }
                    )
                }
                if (grupAEditar != null) {
                    val grupId = grupAEditar!!.first
                    val nomOriginal = grupAEditar!!.second
                    if (editNomGrup.isEmpty()) editNomGrup = nomOriginal // Inicialitza només la primera vegada

                    AlertDialog(
                        onDismissRequest = {
                            grupAEditar = null
                            editNomGrup = ""
                            editClauGrup = ""
                        },
                        title = { Text("Editar grup") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = editNomGrup,
                                    onValueChange = { editNomGrup = it },
                                    label = { Text("Nom del grup") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editClauGrup,
                                    onValueChange = { editClauGrup = it },
                                    label = { Text("Clau del grup (opcional)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val db = FirebaseFirestore.getInstance()
                                    val updates = mutableMapOf<String, Any>("nom" to editNomGrup)
                                    if (editClauGrup.isNotBlank()) updates["clau"] = editClauGrup
                                    db.collection("grups").document(grupId)
                                        .update(updates)
                                        .addOnSuccessListener {
                                            // Refresca la llista
                                            loading = true
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val usuariDoc = db.collection("usuaris").document(user!!.uid).get().await()
                                                val grupIds = (usuariDoc.get("grups") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                                val grupsList = mutableListOf<Pair<String, String>>()
                                                for (gid in grupIds) {
                                                    val grupDoc = db.collection("grups").document(gid).get().await()
                                                    val nom = grupDoc.getString("nom") ?: gid
                                                    grupsList.add(gid to nom)
                                                }
                                                grups = grupsList
                                                loading = false
                                            }
                                            grupAEditar = null
                                            editNomGrup = ""
                                            editClauGrup = ""
                                        }
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Desar") }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = {
                                    grupAEditar = null
                                    editNomGrup = ""
                                    editClauGrup = ""
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Cancel·lar") }
                        }
                    )
                }
            }
        }
    }
}