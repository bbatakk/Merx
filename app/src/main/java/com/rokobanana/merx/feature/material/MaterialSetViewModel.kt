package com.rokobanana.merx.feature.material.set

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.usecase.GetMaterialSetsUseCase
import com.rokobanana.merx.domain.usecase.AddMaterialSetUseCase
import com.rokobanana.merx.domain.usecase.UpdateMaterialSetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialSetViewModel @Inject constructor(
    private val getSets: GetMaterialSetsUseCase,
    private val addSet: AddMaterialSetUseCase,
    private val updateSet: UpdateMaterialSetUseCase
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
            // No reload needed here, MainActivity reloads sets globally
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
}