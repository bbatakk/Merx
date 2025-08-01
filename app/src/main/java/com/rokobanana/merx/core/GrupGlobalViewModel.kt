package com.rokobanana.merx.core

import androidx.lifecycle.ViewModel
import com.rokobanana.merx.domain.model.RolMembre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GrupGlobalViewModel @Inject constructor() : ViewModel() {
    // El grupId global
    private val _grupId = MutableStateFlow<String?>(null)
    val grupId: StateFlow<String?> = _grupId

    // El rol global (opcional)
    private val _userRol = MutableStateFlow<RolMembre?>(null)
    val userRol: StateFlow<RolMembre?> = _userRol

    // Afegim la funció per posar el grupId
    fun setGrupId(grupIdNou: String) {
        _grupId.value = grupIdNou
    }

    // Opcional: funció per posar el rol
    fun setUserRol(rol: RolMembre?) {
        _userRol.value = rol
    }

    // Si vols un reset:
    fun clearGrup() {
        _grupId.value = null
        _userRol.value = null
    }
}