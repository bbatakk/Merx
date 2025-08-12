package com.rokobanana.merx.domain.usecase

import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.domain.repository.MaterialItemRepository
import javax.inject.Inject

class UpdateMaterialItemUseCase @Inject constructor(
    private val repository: MaterialItemRepository
) {
    suspend operator fun invoke(item: MaterialItem) {
        repository.updateItem(item)
    }
}