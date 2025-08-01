package com.rokobanana.merx.domain.usecase

import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.repository.MaterialCollectionRepository
import javax.inject.Inject

class AddMaterialCollectionUseCase @Inject constructor(
    private val repository: MaterialCollectionRepository
) {
    suspend operator fun invoke(collection: MaterialCollection): String =
        repository.addCollection(collection)
}