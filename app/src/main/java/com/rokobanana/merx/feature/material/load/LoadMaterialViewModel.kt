package com.rokobanana.merx.feature.material.load

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoadMaterialViewModel : ViewModel() {
    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedItemIds: StateFlow<Set<String>> = _selectedItemIds

    private val _checkedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val checkedItemIds: StateFlow<Set<String>> = _checkedItemIds

    fun setSelectedItemIds(ids: Set<String>) {
        _selectedItemIds.value = ids
    }

    fun markItems(itemIds: Set<String>, checked: Boolean) {
        _selectedItemIds.value = if (checked) _selectedItemIds.value + itemIds else _selectedItemIds.value - itemIds
    }

    fun markItem(itemId: String, checked: Boolean) {
        _checkedItemIds.value = if (checked) _checkedItemIds.value + itemId else _checkedItemIds.value - itemId
    }

    fun markCheckedItems(itemIds: Set<String>, checked: Boolean) {
        _checkedItemIds.value = if (checked) _checkedItemIds.value + itemIds else _checkedItemIds.value - itemIds
    }

    fun setCheckedItemIds(ids: Set<String>) {
        _checkedItemIds.value = ids
    }

    fun resetSession() {
        _selectedItemIds.value = emptySet()
        _checkedItemIds.value = emptySet()
    }
}