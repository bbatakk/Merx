package com.rokobanana.merx.data.repository

import com.rokobanana.merx.data.remote.FirebaseAuthDataSource
import com.rokobanana.merx.data.remote.FirebaseFirestoreDataSource
import com.rokobanana.merx.domain.model.Usuari
import com.rokobanana.merx.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirebaseFirestoreDataSource
) : AuthRepository {

    override suspend fun usernameExists(nomUsuari: String): Boolean {
        return firestoreDataSource.usernameExists(nomUsuari)
    }

    override suspend fun registerUser(nomComplet: String, nomUsuari: String, email: String, password: String): Usuari {
        val user = authDataSource.createUserWithEmailAndPassword(email.trim().lowercase(), password)
        val nouUsuari = Usuari(
            id = user.uid,
            nomComplet = nomComplet,
            nomUsuari = nomUsuari,
            correu = email.trim().lowercase()
        )
        firestoreDataSource.setUsuari(user.uid, nouUsuari)
        return nouUsuari
    }

    override suspend fun loginWithEmail(email: String, password: String): String? {
        val user = authDataSource.signInWithEmailAndPassword(email, password)
        return user?.uid
    }

    override suspend fun loginWithNomUsuari(nomUsuari: String, password: String): String? {
        val correu = firestoreDataSource.getEmailByNomUsuari(nomUsuari)
            ?: throw Exception("No s'ha trobat el correu")
        val user = authDataSource.signInWithEmailAndPassword(correu, password)
        return user?.uid
    }

    override suspend fun getUsuari(uid: String): Usuari? {
        return firestoreDataSource.getUsuari(uid)
    }

    override suspend fun updateNomComplet(uid: String, nouNomComplet: String) {
        firestoreDataSource.updateNomComplet(uid, nouNomComplet)
    }

    override suspend fun desvincularUsuariDeGrup(uid: String, grupId: String) {
        firestoreDataSource.desvincularUsuariDeGrup(uid, grupId)
    }

    override suspend fun esborrarUsuari(uid: String) {
        firestoreDataSource.esborrarUsuari(uid)
        authDataSource.deleteCurrentUser()
    }

    override fun signOut() {
        authDataSource.signOut()
    }

    override fun getCurrentUser() = authDataSource.currentUser
}