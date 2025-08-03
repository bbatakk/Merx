package com.rokobanana.merx.feature.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.domain.usecase.GetMaterialItemsUseCase
import com.rokobanana.merx.domain.usecase.AddMaterialItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialItemViewModel @Inject constructor(
    private val getItems: GetMaterialItemsUseCase,
    private val addItem: AddMaterialItemUseCase
) : ViewModel() {

    private val _items = MutableStateFlow<List<MaterialItem>>(emptyList())
    val items: StateFlow<List<MaterialItem>> = _items

    fun loadItems(itemIds: List<String>) {
        viewModelScope.launch {
            _items.value = getItems(itemIds)
        }
    }

    fun addNewItem(item: MaterialItem, onResult: (String) -> Unit = {}) {
        viewModelScope.launch {
            val id = addItem(item)
            onResult(id)
        }
    }
}