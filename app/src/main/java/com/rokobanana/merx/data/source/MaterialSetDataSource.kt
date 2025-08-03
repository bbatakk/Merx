package com.rokobanana.merx.data.source

import com.rokobanana.merx.domain.model.MaterialSet

interface MaterialSetDataSource {
    suspend fun getSetsByIds(setIds: List<String>): List<MaterialSet>
    suspend fun addSet(set: MaterialSet): String
    suspend fun updateSet(set: MaterialSet)
    suspend fun deleteSet(setId: String)
}