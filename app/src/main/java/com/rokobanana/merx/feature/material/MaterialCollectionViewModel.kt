package com.rokobanana.merx.feature.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialCollection
import com.rokobanana.merx.domain.usecase.AddMaterialCollectionUseCase
import com.rokobanana.merx.domain.usecase.GetMaterialCollectionsUseCase
import com.rokobanana.merx.domain.usecase.UpdateMaterialCollectionUseCase // <-- Afegit!
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialCollectionViewModel @Inject constructor(
    private val getCollections: GetMaterialCollectionsUseCase,
    private val addCollection: AddMaterialCollectionUseCase,
    private val updateCollection: UpdateMaterialCollectionUseCase // <-- Afegit!
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
            // Torna a carregar les col·leccions del grup associat
            loadCollections(collection.grupId)
        }
    }

    fun addSetToCollection(collectionId: String, setId: String, onResult: (() -> Unit)? = null) {
        viewModelScope.launch {
            val collection = _collections.value.find { it.id == collectionId }
            if (collection != null && !collection.setIds.contains(setId)) {
                val updatedCollection = collection.copy(setIds = collection.setIds + setId)
                updateCollection(updatedCollection)
                // Opcional: recarrega les col·leccions per veure el canvi a la UI
                loadCollections(collection.grupId)
                onResult?.invoke()
            }
        }
    }
}