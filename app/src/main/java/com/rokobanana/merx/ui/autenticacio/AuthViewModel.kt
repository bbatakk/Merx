package com.rokobanana.merx.ui.autenticacio

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rokobanana.merx.data.DataStoreHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AuthViewModel(application: Application) : ViewModel() {
    private val context = application.applicationContext
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dataStoreHelper = DataStoreHelper(context)

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

    fun signOut() {
        auth.signOut()
        viewModelScope.launch {
            dataStoreHelper.clearGrupId()
        }
        _userState.value = null
    }
}

