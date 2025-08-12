package com.rokobanana.merx.feature.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.usecase.AddMaterialCollectionUseCase
import com.rokobanana.merx.domain.usecase.GetMaterialCollectionsUseCase
import com.rokobanana.merx.domain.usecase.UpdateMaterialCollectionUseCase
import com.rokobanana.merx.domain.repository.MaterialCollectionRepository
import com.rokobanana.merx.domain.usecase.DeleteMaterialCollectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialCollectionViewModel @Inject constructor(
    private val getCollections: GetMaterialCollectionsUseCase,
    private val addCollection: AddMaterialCollectionUseCase,
    private val updateCollection: UpdateMaterialCollectionUseCase,
    private val deleteCollectionUseCase: DeleteMaterialCollectionUseCase
) : ViewModel() {

    private val _collections = MutableStateFlow<List<MaterialCollection>>(emptyList())
    val collections: StateFlow<List<MaterialCollection>> = _collections

    fun loadCollections(grupId: String) {
        viewModelScope.launch {
            _collections.value = getCollections(grupId)
        }
    }

    fun addNewCollection(collection: MaterialCollection, onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            val id = addCollection(collection)
            onResult(id)
            loadCollections(collection.grupId)
        }
    }

    fun addSetToCollection(collectionId: String, setId: String, onResult: (() -> Unit)? = null) {
        viewModelScope.launch {
            val collection = _collections.value.find { it.id == collectionId }
            if (collection != null && !collection.setIds.contains(setId)) {
                val updatedCollection = collection.copy(setIds = collection.setIds + setId)
                updateCollection(updatedCollection)
                loadCollections(collection.grupId)
                onResult?.invoke()
            }
        }
    }

    fun updateCollectionName(collection: MaterialCollection, newName: String) {
        viewModelScope.launch {
            val updated = collection.copy(name = newName)
            updateCollection(updated)
            loadCollections(collection.grupId)
        }
    }

    fun deleteCollection(collection: MaterialCollection) {
        viewModelScope.launch {
            deleteCollectionUseCase(collection.id)
            loadCollections(collection.grupId)
        }
    }
}