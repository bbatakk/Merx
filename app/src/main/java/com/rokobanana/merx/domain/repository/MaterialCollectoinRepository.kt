package com.rokobanana.merx.domain.repository

import com.rokobanana.merx.domain.model.MaterialCollection

interface MaterialCollectionRepository {
    suspend fun getCollections(): List<MaterialCollection>
    suspend fun getCollection(id: String): MaterialCollection?
    suspend fun addCollection(collection: MaterialCollection): String // retorna id
    suspend fun updateCollection(collection: MaterialCollection)
    suspend fun deleteCollection(id: String)
}