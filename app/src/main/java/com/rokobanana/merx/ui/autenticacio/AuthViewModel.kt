package com.rokobanana.merx.ui.autenticacio

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _userState = MutableStateFlow(auth.currentUser)
    val userState: StateFlow<com.google.firebase.auth.FirebaseUser?> = _userState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _userState.value = auth.currentUser
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = task.exception?.localizedMessage ?: "Error desconegut"
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
                    _errorMessage.value = task.exception?.localizedMessage ?: "Error desconegut"
                }
            }
    }

    fun logout() {
        auth.signOut()
        _userState.value = null
    }
}
