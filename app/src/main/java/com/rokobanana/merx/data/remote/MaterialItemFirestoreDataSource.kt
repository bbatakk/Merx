package com.rokobanana.merx.data.remote

import com.rokobanana.merx.domain.model.MaterialItem
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.data.source.MaterialItemDataSource
import kotlinx.coroutines.tasks.await

class MaterialItemFirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MaterialItemDataSource {

    private val collectionName = "material_items"

    override suspend fun getItemsByIds(ids: List<String>): List<MaterialItem> {
        if (ids.isEmpty()) return emptyList()
        val snapshots = firestore.collection(collectionName)
            .whereIn("id", ids)
            .get()
            .await()
        return snapshots.documents.mapNotNull { it.toObject(MaterialItem::class.java) }
    }

    override suspend fun addItem(item: MaterialItem): String {
        val id = item.id.ifBlank { firestore.collection(collectionName).document().id }
        val newItem = item.copy(id = id)
        firestore.collection(collectionName).document(id).set(newItem).await()
        return id
    }
}