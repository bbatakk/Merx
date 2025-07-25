package com.rokobanana.merx.feature.autenticacio

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.core.datastore.DataStoreHelper
import com.rokobanana.merx.domain.model.Usuari
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AuthViewModel(application: Application) : ViewModel() {
    private val context = application.applicationContext
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val dataStoreHelper = DataStoreHelper(context)

    private val _userState = MutableStateFlow<Usuari?>(null)
    val userState: StateFlow<Usuari?> = _userState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun register(nomComplet: String, nomUsuari: String, email: String, password: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val usernameExists = !db.collection("usuaris").whereEqualTo("nomUsuari", nomUsuari).get().await().isEmpty
            val emailExists = !db.collection("usuaris").whereEqualTo("correu", email).get().await().isEmpty

            if (usernameExists) {
                _errorMessage.value = "Aquest nom d'usuari ja existeix"
                return@launch
            }
            if (emailExists) {
                _errorMessage.value = "Aquest correu ja existeix"
                return@launch
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        val nouUsuari = Usuari(
                            id = user.uid,
                            nomComplet = nomComplet,
                            nomUsuari = nomUsuari,
                            correu = email
                        )
                        db.collection("usuaris").document(user.uid)
                            .set(nouUsuari)
                            .addOnSuccessListener {
                                _userState.value = nouUsuari
                                _errorMessage.value = null
                            }
                            .addOnFailureListener { e -> _errorMessage.value = "Error guardant usuari: ${e.message}" }
                    }
                }
                .addOnFailureListener { e -> _errorMessage.value = e.message }
        }
    }

    fun login(input: String, password: String) {
        viewModelScope.launch {
            if (input.contains("@")) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(input, password)
                    .addOnSuccessListener { result ->
                        carregarUsuari(result.user?.uid)
                    }
                    .addOnFailureListener { e -> _errorMessage.value = e.message }
            } else {
                val db = FirebaseFirestore.getInstance()
                db.collection("usuaris").whereEqualTo("nomUsuari", input).get()
                    .addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            val correu = result.documents[0].getString("correu") ?: return@addOnSuccessListener
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(correu, password)
                                .addOnSuccessListener { res -> carregarUsuari(res.user?.uid) }
                                .addOnFailureListener { e -> _errorMessage.value = e.message }
                        } else {
                            _errorMessage.value = "Nom d'usuari no existeix"
                        }
                    }
                    .addOnFailureListener { e -> _errorMessage.value = e.message }
            }
        }
    }

    init {
        auth.currentUser?.uid?.let { carregarUsuari(it) }
    }
    private fun carregarUsuari(uid: String?) {
        if (uid == null) return
        FirebaseFirestore.getInstance().collection("usuaris").document(uid).get()
            .addOnSuccessListener { doc ->
                val u = doc.toObject(Usuari::class.java)
                _userState.value = u
            }
    }

    fun updateProfile(nouNomComplet: String, nouNomUsuari: String) {
        val usuariActual = userState.value ?: return
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            // Comprova que el nou nom d'usuari no existeixi (o és el mateix que l'actual)
            val usernameExists = db.collection("usuaris")
                .whereEqualTo("nomUsuari", nouNomUsuari)
                .get().await()
                .documents
                .any { it.id != usuariActual.id }
            if (usernameExists) {
                _errorMessage.value = "Aquest nom d'usuari ja existeix"
                return@launch
            }

            db.collection("usuaris").document(usuariActual.id)
                .update(
                    mapOf(
                        "nomComplet" to nouNomComplet,
                        "nomUsuari" to nouNomUsuari
                    )
                )
                .addOnSuccessListener {
                    // Actualitza l'estat local
                    _userState.value = usuariActual.copy(nomComplet = nouNomComplet, nomUsuari = nouNomUsuari)
                    _errorMessage.value = null
                }
                .addOnFailureListener { e -> _errorMessage.value = "Error actualitzant el perfil: ${e.message}" }
        }
    }

    fun deleteAccountAndUnlinkFromGroups(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid ?: return onError("No user logged in")
        val db = FirebaseFirestore.getInstance()

        // 1. Buscar grups on l'usuari és membre
        db.collection("grups")
            .whereArrayContains("membres", uid)
            .get()
            .addOnSuccessListener { grupsSnapshot ->
                val batch = db.batch()
                grupsSnapshot.documents.forEach { doc ->
                    val grupRef = db.collection("grups").document(doc.id)
                    batch.update(grupRef, "membres", FieldValue.arrayRemove(uid))
                }
                // 2. Esborra el document d'usuari
                val usuariRef = db.collection("usuaris").document(uid)
                batch.delete(usuariRef)

                batch.commit().addOnSuccessListener {
                    // 3. Esborra el compte de Firebase Auth
                    currentUser.delete()
                        .addOnSuccessListener {
                            _userState.value = null
                            onSuccess()
                        }
                        .addOnFailureListener { e -> onError("Error eliminant compte: ${e.message}") }
                }.addOnFailureListener { e -> onError("Error actualitzant grups: ${e.message}") }
            }
            .addOnFailureListener { e -> onError("Error buscant grups: ${e.message}") }
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

    fun signOut() {
        auth.signOut()
        viewModelScope.launch {
            dataStoreHelper.clearGrupId()
        }
        _userState.value = null
    }
}

