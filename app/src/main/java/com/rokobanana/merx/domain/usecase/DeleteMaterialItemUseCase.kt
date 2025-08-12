package com.rokobanana.merx.domain.usecase

import com.rokobanana.merx.domain.repository.MaterialItemRepository
import javax.inject.Inject

class DeleteMaterialItemUseCase @Inject constructor(
    private val repository: MaterialItemRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteItem(id)
    }
}