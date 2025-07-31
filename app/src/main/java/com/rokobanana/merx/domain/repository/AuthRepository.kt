package com.rokobanana.merx.domain.repository

import com.rokobanana.merx.domain.model.Usuari

interface AuthRepository {
    suspend fun usernameExists(nomUsuari: String): Boolean
    suspend fun registerUser(nomComplet: String, nomUsuari: String, email: String, password: String): Usuari
    suspend fun loginWithEmail(email: String, password: String): String?
    suspend fun loginWithNomUsuari(nomUsuari: String, password: String): String?
    suspend fun getUsuari(uid: String): Usuari?
    suspend fun updateNomComplet(uid: String, nouNomComplet: String)
    suspend fun desvincularUsuariDeGrup(uid: String, grupId: String)
    suspend fun esborrarUsuari(uid: String)
    fun signOut()
    fun getCurrentUser(): com.google.firebase.auth.FirebaseUser?
}