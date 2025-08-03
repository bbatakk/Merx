package com.rokobanana.merx.feature.grup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GrupViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Map per guardar el nom de cada grup per id
    private val nomsGrup = mutableMapOf<String, MutableStateFlow<String>>()

    /**
     * Obté el nom real del grup donat el grupId.
     * Si el nom no està carregat, el carrega des de Firestore (col·lecció "grups").
     */
    fun getNomGrup(grupId: String): StateFlow<String> {
        // Si ja tenim el StateFlow, el retornem
        if (nomsGrup.containsKey(grupId)) {
            return nomsGrup[grupId]!!
        }

        // Si no, creem el StateFlow i carreguem el nom des de Firestore
        val flow = MutableStateFlow("")
        nomsGrup[grupId] = flow

        viewModelScope.launch {
            db.collection("grups").document(grupId).get()
                .addOnSuccessListener { document ->
                    val nom = document.getString("nom") ?: ""
                    flow.value = nom
                }
                .addOnFailureListener {
                    flow.value = ""
                }
        }

        return flow
    }
}