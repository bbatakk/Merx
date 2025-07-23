package com.rokobanana.merx.ui.seleccionarGrup

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.ui.autenticacio.AuthViewModel
import com.rokobanana.merx.ui.autenticacio.AuthViewModelFactory
import kotlinx.coroutines.tasks.await

@Composable
fun LlistaGrupsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val user = authViewModel.userState.collectAsState().value
    var grups by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // (id, nom)

    LaunchedEffect(user) {
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val grupsSnapshot = db.collection("usuaris").document(user.uid)
                .collection("grups").get().await()

            grups = grupsSnapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val nom = doc.getString("nom") ?: return@mapNotNull null
                id to nom
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Els teus grups", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(grups) { (id, nom) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(nom, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        // Desvincular-se
                        FirebaseFirestore.getInstance()
                            .collection("usuaris").document(user!!.uid)
                            .collection("grups").document(id)
                            .delete()
                        grups = grups.filterNot { it.first == id }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sortir del grup")
                    }
                }
            }
        }
    }
}