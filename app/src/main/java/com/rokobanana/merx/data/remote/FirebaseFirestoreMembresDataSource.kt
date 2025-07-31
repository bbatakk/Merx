package com.rokobanana.merx.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.domain.model.Membre
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFirestoreMembresDataSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val colRef get() = db.collection("membres")

    suspend fun afegirMembre(membre: Membre) {
        colRef.add(membre).await()
    }

    suspend fun membresDeGrup(grupId: String): List<Membre> {
        val snapshot = colRef.whereEqualTo("grupId", grupId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Membre::class.java) }
    }

    suspend fun grupsDeUsuari(usuariId: String): List<Membre> {
        val snapshot = colRef.whereEqualTo("usuariId", usuariId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Membre::class.java) }
    }

    suspend fun eliminaMembre(usuariId: String, grupId: String) {
        val snapshot = colRef
            .whereEqualTo("usuariId", usuariId)
            .whereEqualTo("grupId", grupId)
            .get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }
}