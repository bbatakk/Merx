package com.rokobanana.merx.data.repository

import com.rokobanana.merx.data.source.MaterialSetDataSource
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.repository.MaterialSetRepository
import javax.inject.Inject

class MaterialSetRepositoryImpl @Inject constructor(
    private val dataSource: MaterialSetDataSource
) : MaterialSetRepository {
    override suspend fun getSetsByIds(setIds: List<String>): List<MaterialSet> =
        dataSource.getSetsByIds(setIds)

    override suspend fun addSet(set: MaterialSet): String =
        dataSource.addSet(set)

    override suspend fun updateSet(set: MaterialSet) =
        dataSource.updateSet(set)

    override suspend fun deleteSet(setId: String) =
        dataSource.deleteSet(setId)
}