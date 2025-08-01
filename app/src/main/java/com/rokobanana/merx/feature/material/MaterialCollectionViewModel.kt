package com.rokobanana.merx.feature.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.usecase.AddMaterialCollectionUseCase
import com.rokobanana.merx.domain.usecase.GetMaterialCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialCollectionViewModel @Inject constructor(
    private val getCollections: GetMaterialCollectionsUseCase,
    private val addCollection: AddMaterialCollectionUseCase
) : ViewModel() {

    private val _collections = MutableStateFlow<List<MaterialCollection>>(emptyList())
    val collections: StateFlow<List<MaterialCollection>> = _collections

    fun loadCollections(grupId: String) {
        viewModelScope.launch {
            _collections.value = getCollections(grupId)
        }
    }

    fun addNewCollection(grupId: String, collection: MaterialCollection, onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            val id = addCollection(grupId, collection)
            onResult(id)
            loadCollections(grupId)
        }
    }
}