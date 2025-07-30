package com.rokobanana.merx.feature.autenticacio

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.core.datastore.DataStoreHelper
import com.rokobanana.merx.domain.model.Usuari
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dataStoreHelper = DataStoreHelper(context)

    private val _userState = MutableStateFlow<Usuari?>(null)
    val userState: StateFlow<Usuari?> = _userState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        auth.currentUser?.uid?.let { carregarUsuari(it) }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun register(nomComplet: String, nomUsuari: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val usernameExists = !db.collection("usuaris").whereEqualTo("nomUsuari", nomUsuari).get().await().isEmpty

                if (usernameExists) {
                    _errorMessage.value = "Aquest nom d'usuari ja existeix"
                    return@launch
                }

                // Firebase Auth ja fa el control d'email únic!
                val result = auth.createUserWithEmailAndPassword(email.trim().lowercase(), password).await()
                val user = result.user ?: throw Exception("No s'ha creat l'usuari")
                val nouUsuari = Usuari(
                    id = user.uid,
                    nomComplet = nomComplet,
                    nomUsuari = nomUsuari,
                    correu = email.trim().lowercase()
                )
                db.collection("usuaris").document(user.uid).set(nouUsuari).await()
                _userState.value = nouUsuari
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
                if (input.contains("@")) {
                    val result = FirebaseAuth.getInstance().signInWithEmailAndPassword(input, password).await()
                    carregarUsuari(result.user?.uid)
                } else {
                    val db = FirebaseFirestore.getInstance()
                    val result = db.collection("usuaris").whereEqualTo("nomUsuari", input).get().await()
                    if (!result.isEmpty) {
                        val correu = result.documents[0].getString("correu") ?: throw Exception("No s'ha trobat el correu")
                        val res = FirebaseAuth.getInstance().signInWithEmailAndPassword(correu, password).await()
                        carregarUsuari(res.user?.uid)
                    } else {
                        _errorMessage.value = "Nom d'usuari no existeix"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun carregarUsuari(uid: String?) {
        if (uid == null) return
        db.collection("usuaris").document(uid).get()
            .addOnSuccessListener { doc ->
                val u = doc.toObject(Usuari::class.java)
                _userState.value = u
            }
            .addOnFailureListener { e ->
                _errorMessage.value = tradueixError(e)
                signOut()
            }
    }

    fun updateProfile(nouNomComplet: String) {
        val usuariActual = userState.value ?: return
        viewModelScope.launch {
            try {
                db.collection("usuaris").document(usuariActual.id)
                    .update("nomComplet", nouNomComplet)
                    .await()
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
                val snapshot = db.collection("membres")
                    .whereEqualTo("usuariId", uid)
                    .whereEqualTo("grupId", grupId)
                    .get().await()
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                onSuccess()
            } catch (e: Exception) {
                onError(tradueixError(e))
            }
        }
    }

    fun esborrarUsuari(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            val uid = currentUser?.uid ?: return@launch onError("No user logged in")
            try {
                val snapshot = db.collection("membres").whereEqualTo("usuariId", uid).get().await()
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                val usuariRef = db.collection("usuaris").document(uid)
                batch.delete(usuariRef)
                batch.commit().await()
                currentUser.delete().await()
                _userState.value = null
                onSuccess()
            } catch (e: Exception) {
                onError(tradueixError(e))
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