package com.rokobanana.merx.data.source

import com.rokobanana.merx.domain.model.MaterialItem

interface MaterialItemDataSource {
    suspend fun getItemsByIds(ids: List<String>): List<MaterialItem>
    suspend fun addItem(item: MaterialItem): String
}