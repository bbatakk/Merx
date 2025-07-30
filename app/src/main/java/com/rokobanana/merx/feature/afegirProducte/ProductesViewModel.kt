package com.rokobanana.merx.feature.afegirProducte

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.rokobanana.merx.data.repository.ProductesRepository
import com.rokobanana.merx.domain.model.Producte
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductesViewModel @Inject constructor(
    private val repository: ProductesRepository,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // GrupId passat via arguments de navegació
    private val grupId: String = savedStateHandle["grupId"] ?: ""

    private val _productes = MutableStateFlow<List<Producte>>(emptyList())
    val productes: StateFlow<List<Producte>> = _productes

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { carregarProductes() }

    fun carregarProductes() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _productes.value = repository.getProductes(grupId)
                _loading.value = false
            } catch (e: Exception) {
                _error.value = e.localizedMessage
                _loading.value = false
            }
        }
    }

    fun afegirProducte(producte: Producte) = viewModelScope.launch {
        try {
            repository.afegirProducte(grupId, producte)
            carregarProductes()
        } catch (e: Exception) {
            _error.value = e.localizedMessage
        }
    }

    fun updateEstoc(producteId: String, talla: String, nouEstoc: Int) = viewModelScope.launch {
        try {
            repository.updateEstoc(grupId, producteId, talla, nouEstoc)
            carregarProductes()
        } catch (e: Exception) {
            _error.value = e.localizedMessage
        }
    }

    fun updatePreu(producteId: String, nouPreu: Double) = viewModelScope.launch {
        try {
            repository.updatePreu(grupId, producteId, nouPreu)
            carregarProductes()
        } catch (e: Exception) {
            _error.value = e.localizedMessage
        }
    }

    fun eliminarProducte(producteId: String) = viewModelScope.launch {
        try {
            repository.eliminarProducte(grupId, producteId)
            carregarProductes()
        } catch (e: Exception) {
            _error.value = e.localizedMessage
        }
    }

    fun clearError() {
        _error.value = null
    }
}