package com.rokobanana.merx.data.repository

import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.domain.repository.MaterialItemRepository
import com.rokobanana.merx.data.source.MaterialItemDataSource

class MaterialItemRepositoryImpl(
    private val dataSource: MaterialItemDataSource
) : MaterialItemRepository {
    override suspend fun getItemsByIds(ids: List<String>): List<MaterialItem> {
        return dataSource.getItemsByIds(ids)
    }
    override suspend fun addItem(item: MaterialItem): String {
        return dataSource.addItem(item)
    }
}