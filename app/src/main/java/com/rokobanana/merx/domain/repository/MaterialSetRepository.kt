package com.rokobanana.merx.domain.repository

import com.rokobanana.merx.domain.model.MaterialSet

interface MaterialSetRepository {
    suspend fun getSets(collectionId: String): List<MaterialSet>
    suspend fun addSet(set: MaterialSet): String
    suspend fun updateSet(set: MaterialSet)
    suspend fun deleteSet(setId: String)
}