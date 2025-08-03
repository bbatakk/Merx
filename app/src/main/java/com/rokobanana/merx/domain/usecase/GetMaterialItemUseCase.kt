package com.rokobanana.merx.domain.usecase

import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.domain.repository.MaterialItemRepository
import javax.inject.Inject

class GetMaterialItemsUseCase @Inject constructor(
    private val repository: MaterialItemRepository
) {
    suspend operator fun invoke(ids: List<String>): List<MaterialItem> {
        return repository.getItemsByIds(ids)
    }
}