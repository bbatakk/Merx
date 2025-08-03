package com.rokobanana.merx.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.data.source.MaterialSetDataSource
import com.rokobanana.merx.domain.model.MaterialSet
import kotlinx.coroutines.tasks.await

class MaterialSetFirestoreDataSource(
    private val firestore: FirebaseFirestore
) : MaterialSetDataSource {

    override suspend fun getSets(collectionId: String): List<MaterialSet> {
        val snapshot = firestore.collection("materialSets")
            .whereEqualTo("collectionId", collectionId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(MaterialSet::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun addSet(set: MaterialSet): String {
        val ref = firestore.collection("materialSets").document()
        ref.set(set.copy(id = ref.id)).await()
        return ref.id
    }

    override suspend fun updateSet(set: MaterialSet) {
        firestore.collection("materialSets").document(set.id).set(set).await()
    }

    override suspend fun deleteSet(setId: String) {
        firestore.collection("materialSets").document(setId).delete().await()
    }
}