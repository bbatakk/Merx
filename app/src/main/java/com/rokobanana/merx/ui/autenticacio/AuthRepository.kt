package com.rokobanana.merx.feature.autenticacio

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.domain.model.Usuari
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun usernameExists(nomUsuari: String): Boolean {
        return !db.collection("usuaris").whereEqualTo("nomUsuari", nomUsuari).get().await().isEmpty
    }

    suspend fun registerUser(nomComplet: String, nomUsuari: String, email: String, password: String): Usuari {
        val result = auth.createUserWithEmailAndPassword(email.trim().lowercase(), password).await()
        val user = result.user ?: throw Exception("No s'ha creat l'usuari")
        val nouUsuari = Usuari(
            id = user.uid,
            nomComplet = nomComplet,
            nomUsuari = nomUsuari,
            correu = email.trim().lowercase()
        )
        db.collection("usuaris").document(user.uid).set(nouUsuari).await()
        return nouUsuari
    }

    suspend fun loginWithEmail(email: String, password: String): String? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid
    }

    suspend fun loginWithNomUsuari(nomUsuari: String, password: String): String? {
        val result = db.collection("usuaris").whereEqualTo("nomUsuari", nomUsuari).get().await()
        if (!result.isEmpty) {
            val correu = result.documents[0].getString("correu") ?: throw Exception("No s'ha trobat el correu")
            val res = auth.signInWithEmailAndPassword(correu, password).await()
            return res.user?.uid
        } else {
            throw Exception("Nom d'usuari no existeix")
        }
    }

    suspend fun getUsuari(uid: String): Usuari? {
        val doc = db.collection("usuaris").document(uid).get().await()
        return doc.toObject(Usuari::class.java)
    }

    suspend fun updateNomComplet(uid: String, nouNomComplet: String) {
        db.collection("usuaris").document(uid).update("nomComplet", nouNomComplet).await()
    }

    suspend fun desvincularUsuariDeGrup(uid: String, grupId: String) {
        val snapshot = db.collection("membres")
            .whereEqualTo("usuariId", uid)
            .whereEqualTo("grupId", grupId)
            .get().await()
        val batch = db.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    suspend fun esborrarUsuari(uid: String) {
        val snapshot = db.collection("membres").whereEqualTo("usuariId", uid).get().await()
        val batch = db.batch()
        for (doc in snapshot.documents) {
            batch.delete(doc.reference)
        }
        val usuariRef = db.collection("usuaris").document(uid)
        batch.delete(usuariRef)
        batch.commit().await()
        auth.currentUser?.delete()?.await()
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}