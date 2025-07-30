package com.rokobanana.merx.feature.afegirProducte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.rokobanana.merx.domain.model.Producte
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductesViewModel(grupId: String) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _productes = MutableStateFlow<List<Producte>>(emptyList())
    val productes: StateFlow<List<Producte>> = _productes

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val colRef = db.collection("grups").document(grupId).collection("productes")

    private var listenerRegistration: ListenerRegistration? = null

    init {
        _loading.value = true
        listenerRegistration = colRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                _productes.value = emptyList()
                _error.value = error?.localizedMessage
                _loading.value = false
                return@addSnapshotListener
            }
            _productes.value = snapshot.toObjects(Producte::class.java)
            _loading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun getProducte(id: String): Flow<Producte?> = productes.map { list -> list.find { it.id == id } }

    fun updateEstoc(id: String, talla: String, nouEstoc: Int) {
        viewModelScope.launch {
            try {
                colRef.document(id)
                    .update("estocPerTalla.$talla", nouEstoc)
                    .await()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun updatePreu(id: String, nouPreu: Double) {
        viewModelScope.launch {
            try {
                colRef.document(id).update("preu", nouPreu).await()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun eliminarProducte(id: String) {
        viewModelScope.launch {
            try {
                colRef.document(id).delete().await()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun afegirProducte(producte: Producte) {
        viewModelScope.launch {
            try {
                val doc = colRef.document()
                val producteComplet = producte.copy(id = doc.id)
                doc.set(producteComplet).await()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}