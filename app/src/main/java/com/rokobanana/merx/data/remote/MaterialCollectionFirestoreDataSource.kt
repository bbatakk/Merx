package com.rokobanana.merx.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.data.source.MaterialCollectionDataSource
import com.rokobanana.merx.domain.model.MaterialCollection
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MaterialCollectionFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : MaterialCollectionDataSource {

    private val collectionRef = firestore.collection("material_collections")

    // Ara rep grupId per filtrar
    override suspend fun getCollections(grupId: String): List<MaterialCollection> {
        return collectionRef
            .whereEqualTo("grupId", grupId) // <-- filtra per grupId
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(MaterialCollection::class.java)?.copy(id = it.id) }
    }

    override suspend fun getCollection(id: String): MaterialCollection? {
        val doc = collectionRef.document(id).get().await()
        return doc.toObject(MaterialCollection::class.java)?.copy(id = doc.id)
    }

    override suspend fun addCollection(collection: MaterialCollection): String {
        val docRef = collectionRef.add(collection).await()
        return docRef.id
    }

    override suspend fun updateCollection(collection: MaterialCollection) {
        collectionRef.document(collection.id).set(collection).await()
    }

    override suspend fun deleteCollection(id: String) {
        collectionRef.document(id).delete().await()
    }
}