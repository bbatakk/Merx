package com.rokobanana.merx.domain.repository

import com.rokobanana.merx.domain.model.MaterialSet
import kotlinx.coroutines.flow.Flow

interface MaterialSetRepository {
    fun getAllSets(grupId: String): Flow<List<MaterialSet>>
    suspend fun addSet(grupId: String, set: MaterialSet): String
    suspend fun updateSet(grupId: String, set: MaterialSet)
    suspend fun deleteSet(grupId: String, setId: String)
    suspend fun getSetById(grupId: String, setId: String): MaterialSet // <--- AFEGIT
}