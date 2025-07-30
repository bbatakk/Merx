package com.rokobanana.merx.feature.autenticacio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.core.datastore.DataStoreHelper
import com.rokobanana.merx.feature.autenticacio.AuthRepository
import com.rokobanana.merx.domain.model.Usuari
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val dataStoreHelper: DataStoreHelper
) : ViewModel() {

    private val _userState = MutableStateFlow<Usuari?>(null)
    val userState: StateFlow<Usuari?> = _userState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        repository.getCurrentUser()?.uid?.let { carregarUsuari(it) }
    }

    fun register(nomComplet: String, nomUsuari: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val usernameExists = repository.usernameExists(nomUsuari)
                if (usernameExists) {
                    _errorMessage.value = "Aquest nom d'usuari ja existeix"
                    return@launch
                }
                val usuari = repository.registerUser(nomComplet, nomUsuari, email, password)
                _userState.value = usuari
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = tradueixError(e)
            }
        }
    }

    fun login(input: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = if (input.contains("@")) {
                    repository.loginWithEmail(input, password)
                } else {
                    repository.loginWithNomUsuari(input, password)
                }
                carregarUsuari(uid)
            } catch (e: Exception) {
                _errorMessage.value = tradueixError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun carregarUsuari(uid: String?) {
        if (uid == null) return
        viewModelScope.launch {
            try {
                val u = repository.getUsuari(uid)
                _userState.value = u
            } catch (e: Exception) {
                _errorMessage.value = tradueixError(e)
                signOut()
            }
        }
    }

    fun updateProfile(nouNomComplet: String) {
        val usuariActual = userState.value ?: return
        viewModelScope.launch {
            try {
                repository.updateNomComplet(usuariActual.id, nouNomComplet)
                _userState.value = usuariActual.copy(nomComplet = nouNomComplet)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error actualitzant el perfil: ${e.message}"
            }
        }
    }

    fun desvincularUsuariDeGrup(uid: String, grupId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.desvincularUsuariDeGrup(uid, grupId)
                onSuccess()
            } catch (e: Exception) {
                onError(tradueixError(e))
            }
        }
    }

    fun esborrarUsuari(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            val uid = currentUser?.uid ?: return@launch onError("No user logged in")
            try {
                repository.esborrarUsuari(uid)
                _userState.value = null
                onSuccess()
            } catch (e: Exception) {
                onError(tradueixError(e))
            }
        }
    }

    fun signOut() {
        repository.signOut()
        viewModelScope.launch {
            dataStoreHelper.clearGrupId()
        }
        _userState.value = null
    }

    private fun tradueixError(exception: Exception?): String {
        val msg = exception?.localizedMessage.orEmpty()
        return when {
            msg.contains("password is invalid", ignoreCase = true) -> "La contrasenya ha de contenir almenys 6 caràcters."
            msg.contains("email address is badly formatted", ignoreCase = true) -> "El correu electrònic no té un format vàlid."
            msg.contains("incorrect password", ignoreCase = true) -> "Contrasenya incorrecta."
            msg.contains("The supplied auth credential is incorrect, malformed or has expired", ignoreCase = true) -> "L'usuari o la contrasenya són incorrectes."
            msg.contains("no user record", ignoreCase = true) ||
                    msg.contains("user does not exist", ignoreCase = true) -> "L'usuari no existeix."
            msg.contains("already in use", ignoreCase = true) -> "Aquest correu ja està registrat."
            msg.contains("network", ignoreCase = true) -> "Error de connexió."
            else -> "Error: $msg"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}