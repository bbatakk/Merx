package com.rokobanana.merx.feature.afegirProducte

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.domain.model.Producte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProductesViewModel(grupId: String) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _productes = MutableStateFlow<List<Producte>>(emptyList())
    val productes: StateFlow<List<Producte>> = _productes

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading
    private val colRef = db.collection("grups").document(grupId).collection("productes")

    init {
        _loading.value = true
        colRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                _productes.value = emptyList()
                _loading.value = false
                return@addSnapshotListener
            }
            _productes.value = snapshot.toObjects(Producte::class.java)
            _loading.value = false
        }
    }

    fun getProducte(id: String): Flow<Producte?> = flow {
        try {
            val snapshot = colRef.document(id).get().await()
            emit(snapshot.toObject(Producte::class.java))
        } catch (_: Exception) {
            emit(null)
        }
    }

    fun updateEstoc(id: String, talla: String, nouEstoc: Int) {
        colRef.document(id)
            .update("estocPerTalla.$talla", nouEstoc)
    }

    fun eliminarProducte(id: String) {
        colRef.document(id).delete()
    }

    fun afegirProducte(producte: Producte) {
        val doc = colRef.document()
        val producteComplet = producte.copy(id = doc.id)
        doc.set(producteComplet)
    }
}