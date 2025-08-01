package com.rokobanana.merx.data.source

import com.rokobanana.merx.domain.model.MaterialCollection

interface MaterialCollectionDataSource {
    suspend fun getCollections(): List<MaterialCollection>
    suspend fun getCollection(id: String): MaterialCollection?
    suspend fun addCollection(collection: MaterialCollection): String
    suspend fun updateCollection(collection: MaterialCollection)
    suspend fun deleteCollection(id: String)
}