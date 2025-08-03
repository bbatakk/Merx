package com.rokobanana.merx.domain.repository

import com.rokobanana.merx.domain.model.MaterialSet

interface MaterialSetRepository {
    // Nova signatura!
    suspend fun getSetsByIds(setIds: List<String>): List<MaterialSet>
    suspend fun addSet(set: MaterialSet): String
    suspend fun updateSet(set: MaterialSet)
    suspend fun deleteSet(setId: String)
}