package com.rokobanana.merx.domain.usecase

import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.repository.MaterialSetRepository
import javax.inject.Inject

class AddMaterialSetUseCase @Inject constructor(
    private val repo: MaterialSetRepository
) {
    suspend operator fun invoke(set: MaterialSet): String = repo.addSet(set)
}