package com.rokobanana.merx.feature.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.domain.usecase.GetMaterialItemsUseCase
import com.rokobanana.merx.domain.usecase.AddMaterialItemUseCase
import com.rokobanana.merx.domain.usecase.UpdateMaterialItemUseCase
import com.rokobanana.merx.domain.usecase.DeleteMaterialItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialItemViewModel @Inject constructor(
    private val getItems: GetMaterialItemsUseCase,
    private val addItem: AddMaterialItemUseCase,
    private val updateItem: UpdateMaterialItemUseCase,
    private val deleteItem: DeleteMaterialItemUseCase
) : ViewModel() {

    private val _allItems = MutableStateFlow<List<MaterialItem>>(emptyList())
    val allItems: StateFlow<List<MaterialItem>> = _allItems

    fun loadItemsByIds(itemIds: List<String>) {
        viewModelScope.launch {
            _allItems.value = getItems(itemIds)
        }
    }

    fun addNewItem(item: MaterialItem, onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            val id = addItem(item)
            onResult(id)
        }
    }

    fun updateItem(
        item: MaterialItem,
        nom: String,
        marca: String,
        model: String,
        descripcio: String,
        quantitat: Int,
        loadedItemIds: List<String>
    ) {
        viewModelScope.launch {
            val updated = item.copy(
                nom = nom,
                marca = marca,
                model = model,
                descripcio = descripcio,
                quantitat = quantitat
            )
            updateItem(updated)
            loadItemsByIds(loadedItemIds)
        }
    }

    fun deleteItem(item: MaterialItem, loadedItemIds: List<String>) {
        viewModelScope.launch {
            deleteItem(item.id)
            loadItemsByIds(loadedItemIds)
        }
    }
}