package com.rokobanana.merx.feature.afegirProducte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.Producte
import com.rokobanana.merx.domain.model.RolMembre
import com.rokobanana.merx.domain.repository.ProductesRepository
import com.rokobanana.merx.domain.repository.MembresRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductesViewModel @Inject constructor(
    private val productesRepository: ProductesRepository,
    private val membresRepository: MembresRepository
) : ViewModel() {

    private val _productes = MutableStateFlow<List<Producte>>(emptyList())
    val productes: StateFlow<List<Producte>> = _productes

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    // NOVA IMPLEMENTACIÓ: rol complet de l'usuari
    private val _userRol = MutableStateFlow<RolMembre?>(null)
    val userRol: StateFlow<RolMembre?> = _userRol

    fun carregarProductes(grupId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val llista = productesRepository.getProductes(grupId)
                println("Productes carregats: $llista")
                _productes.value = llista
                _error.value = null
            } catch (e: Exception) {
                _productes.value = emptyList()
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun carregarRol(grupId: String, usuariId: String?) {
        viewModelScope.launch {
            _isAdmin.value = false
            _userRol.value = null
            println("DEBUG carregarRol: grupId=$grupId, usuariId=$usuariId")
            if (usuariId != null) {
                try {
                    val membres = membresRepository.membresDeGrup(grupId)
                    val membre = membres.find { it.usuariId == usuariId }
                    println("DEBUG membre trobat: $membre")
                    println("DEBUG rol membre: ${membre?.rol}")
                    _isAdmin.value = membre?.rol == RolMembre.ADMIN
                    _userRol.value = membre?.rol // <-- ara pots accedir al rol exacte
                    println("DEBUG isAdmin actualitzat: ${_isAdmin.value}")
                } catch (e: Exception) {
                    println("DEBUG carregarRol: exception ${e.localizedMessage}")
                    _isAdmin.value = false
                    _userRol.value = null
                }
            }
        }
    }

    fun clearRol() {
        _isAdmin.value = false
    }

    fun afegirProducte(producte: Producte, grupId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                productesRepository.afegirProducte(grupId, producte)
                carregarProductes(grupId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateEstoc(id: String, talla: String, nouEstoc: Int, grupId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                productesRepository.updateEstoc(grupId, id, talla, nouEstoc)
                carregarProductes(grupId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun updatePreu(id: String, nouPreu: Double, grupId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                productesRepository.updatePreu(grupId, id, nouPreu)
                carregarProductes(grupId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun eliminarProducte(id: String, grupId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                productesRepository.eliminarProducte(grupId, id)
                carregarProductes(grupId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun getProducte(id: String): Flow<Producte?> =
        productes.map { list -> list.find { it.id == id } }

    fun clearError() {
        _error.value = null
    }
}