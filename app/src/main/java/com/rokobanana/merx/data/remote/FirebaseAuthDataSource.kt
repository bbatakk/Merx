package com.rokobanana.merx.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun createUserWithEmailAndPassword(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("No s'ha creat l'usuari")
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    fun signOut() = auth.signOut()

    fun deleteCurrentUser() {
        auth.currentUser?.delete()
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser
}