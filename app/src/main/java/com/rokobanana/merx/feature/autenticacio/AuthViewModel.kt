package com.rokobanana.merx.feature.autenticacio

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rokobanana.merx.core.datastore.DataStoreHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AuthViewModel(application: Application) : ViewModel() {
    private val context = application.applicationContext
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dataStoreHelper = DataStoreHelper(context)

    private val _userState = MutableStateFlow(auth.currentUser)
    val userState: StateFlow<FirebaseUser?> = _userState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _userState.value = auth.currentUser
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = tradueixError(task.exception)
                }
            }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _userState.value = auth.currentUser
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = tradueixError(task.exception)
                }
            }
    }

    // Nova funció per traduir errors:
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

    fun signOut() {
        auth.signOut()
        viewModelScope.launch {
            dataStoreHelper.clearGrupId()
        }
        _userState.value = null
    }
}

