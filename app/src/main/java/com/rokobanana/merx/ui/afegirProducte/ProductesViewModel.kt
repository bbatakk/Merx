package com.rokobanana.merx.ui.afegirProducte

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.model.Producte
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProductesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _productes = MutableStateFlow<List<Producte>>(emptyList())
    val productes: StateFlow<List<Producte>> = _productes

    init {
        db.collection("productes").addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Aquí podries loguejar o guardar un error en un altre `StateFlow`
                return@addSnapshotListener
            }
            _productes.value = snapshot?.toObjects(Producte::class.java) ?: emptyList()
        }
    }

    fun getProducte(id: String): Flow<Producte?> = flow {
        try {
            val snapshot = db.collection("productes").document(id).get().await()
            emit(snapshot.toObject(Producte::class.java))
        } catch (e: Exception) {
            emit(null) // Evita trencar la UI
        }
    }

    fun updateEstoc(id: String, talla: String, nouEstoc: Int) {
        db.collection("productes").document(id)
            .update("estocPerTalla.$talla", nouEstoc)
    }

    fun guardarCanvis(id: String) {
        // ja s'ha anat guardant directament amb update
    }

    fun eliminarProducte(id: String) {
        db.collection("productes").document(id).delete()
    }

    fun afegirProducte(producte: Producte) {
        val doc = db.collection("productes").document()
        val producteComplet = producte.copy(id = doc.id)
        db.document(doc.path).set(producteComplet)
    }
}
