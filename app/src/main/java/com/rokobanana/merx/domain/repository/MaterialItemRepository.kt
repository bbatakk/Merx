package com.rokobanana.merx.domain.repository

import com.rokobanana.merx.domain.model.MaterialItem

interface MaterialItemRepository {
    suspend fun getItemsByIds(ids: List<String>): List<MaterialItem>
    suspend fun addItem(item: MaterialItem): String
    suspend fun updateItem(item: MaterialItem)      // <-- AFEGIT
    suspend fun deleteItem(id: String)              // <-- AFEGIT
}