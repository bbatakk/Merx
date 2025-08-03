package com.rokobanana.merx.feature.material.set

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.usecase.GetMaterialSetsUseCase
import com.rokobanana.merx.domain.usecase.AddMaterialSetUseCase
import com.rokobanana.merx.domain.usecase.UpdateMaterialSetUseCase // <-- afegit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialSetViewModel @Inject constructor(
    private val getSets: GetMaterialSetsUseCase,
    private val addSet: AddMaterialSetUseCase,
    private val updateSet: UpdateMaterialSetUseCase // <-- afegit
) : ViewModel() {

    private val _sets = MutableStateFlow<List<MaterialSet>>(emptyList())
    val sets: StateFlow<List<MaterialSet>> = _sets

    fun loadSets(collectionId: String) {
        viewModelScope.launch {
            _sets.value = getSets(collectionId)
        }
    }

    fun addNewSet(set: MaterialSet, onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            val id = addSet(set)
            onResult(id)
            loadSets(set.collectionId)
        }
    }

    fun addItemToSet(setId: String, itemId: String) {
        viewModelScope.launch {
            // Troba el set actual
            val set = _sets.value.find { it.id == setId }
            if (set != null && !set.itemIds.contains(itemId)) {
                val updatedSet = set.copy(itemIds = set.itemIds + itemId)
                updateSet(updatedSet)
                loadSets(set.collectionId)
            }
        }
    }
}