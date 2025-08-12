package com.rokobanana.merx.feature.material.set

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.usecase.GetMaterialSetsUseCase
import com.rokobanana.merx.domain.usecase.AddMaterialSetUseCase
import com.rokobanana.merx.domain.usecase.UpdateMaterialSetUseCase
import com.rokobanana.merx.domain.usecase.DeleteMaterialSetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialSetViewModel @Inject constructor(
    private val getSets: GetMaterialSetsUseCase,
    private val addSet: AddMaterialSetUseCase,
    private val updateSet: UpdateMaterialSetUseCase,
    private val deleteSet: DeleteMaterialSetUseCase
) : ViewModel() {

    private val _allSets = MutableStateFlow<List<MaterialSet>>(emptyList())
    val allSets: StateFlow<List<MaterialSet>> = _allSets

    fun loadSetsByIds(setIds: List<String>) {
        viewModelScope.launch {
            _allSets.value = getSets(setIds)
        }
    }

    fun addNewSet(set: MaterialSet, onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            val id = addSet(set)
            onResult(id)
        }
    }

    fun addItemToSet(setId: String, itemId: String) {
        viewModelScope.launch {
            val sets = _allSets.value.toMutableList()
            val idx = sets.indexOfFirst { it.id == setId }
            if (idx != -1) {
                val set = sets[idx]
                val updatedSet = set.copy(itemIds = set.itemIds + itemId)
                updateSet(updatedSet)
                sets[idx] = updatedSet
                _allSets.value = sets
            }
        }
    }

    fun updateSetName(set: MaterialSet, newName: String, loadedSetIds: List<String>) {
        viewModelScope.launch {
            val updated = set.copy(nom = newName)
            updateSet(updated)
            loadSetsByIds(loadedSetIds)
        }
    }

    fun deleteSet(set: MaterialSet, loadedSetIds: List<String>) {
        viewModelScope.launch {
            deleteSet(set.id)
            loadSetsByIds(loadedSetIds)
        }
    }
}