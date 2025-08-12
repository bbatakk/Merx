package com.rokobanana.merx.domain.usecase

import com.rokobanana.merx.domain.repository.MaterialSetRepository
import javax.inject.Inject

class DeleteMaterialSetUseCase @Inject constructor(
    private val repository: MaterialSetRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteSet(id)
    }
}