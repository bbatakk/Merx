package com.rokobanana.merx.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.repository.MaterialSetRepository
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MaterialSetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MaterialSetRepository {

    override fun getAllSets(grupId: String): Flow<List<MaterialSet>> = callbackFlow {
        val ref = firestore.collection("grups").document(grupId).collection("materialSets")
        val registration = ref.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val sets = snapshot.documents.mapNotNull { it.toObject(MaterialSet::class.java)?.copy(id = it.id) }
                trySend(sets)
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addSet(grupId: String, set: MaterialSet): String {
        val docRef = firestore.collection("grups").document(grupId).collection("materialSets").document()
        val nouSet = set.copy(id = docRef.id)
        docRef.set(nouSet).await()
        return docRef.id
    }

    override suspend fun updateSet(grupId: String, set: MaterialSet) {
        val ref = firestore.collection("grups").document(grupId).collection("materialSets").document(set.id)
        ref.set(set, SetOptions.merge()).await()
    }

    override suspend fun deleteSet(grupId: String, setId: String) {
        val ref = firestore.collection("grups").document(grupId).collection("materialSets").document(setId)
        ref.delete().await()
    }

    // Opcional, però molt recomanable!
    override suspend fun getSetById(grupId: String, setId: String): MaterialSet {
        val doc = firestore.collection("grups").document(grupId).collection("materialSets").document(setId).get().await()
        return doc.toObject(MaterialSet::class.java)?.copy(id = doc.id)
            ?: throw Exception("Set not found")
    }
}