package com.rokobanana.merx.domain.usecase

import com.rokobanana.merx.domain.repository.MaterialCollectionRepository
import javax.inject.Inject

class DeleteMaterialCollectionUseCase @Inject constructor(
    private val repository: MaterialCollectionRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteCollection(id)
    }
}