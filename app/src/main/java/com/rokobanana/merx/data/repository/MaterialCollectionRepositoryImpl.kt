package com.rokobanana.merx.data.repository

import com.rokobanana.merx.data.source.MaterialCollectionDataSource
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.repository.MaterialCollectionRepository
import jakarta.inject.Inject

class MaterialCollectionRepositoryImpl @Inject constructor(
    private val dataSource: MaterialCollectionDataSource
) : MaterialCollectionRepository {

    override suspend fun getCollections(grupId: String): List<MaterialCollection> =
        dataSource.getCollections(grupId) // <-- ara filtra per grupId

    override suspend fun getCollection(id: String): MaterialCollection? =
        dataSource.getCollection(id)

    override suspend fun addCollection(collection: MaterialCollection): String =
        dataSource.addCollection(collection)

    override suspend fun updateCollection(collection: MaterialCollection) =
        dataSource.updateCollection(collection)

    override suspend fun deleteCollection(id: String) =
        dataSource.deleteCollection(id)
}