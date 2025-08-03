package com.rokobanana.merx.feature.material.load

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoadMaterialViewModel : ViewModel() {
    // Un Set d’ids dels items seleccionats (i marcats al checklist)
    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedItemIds: StateFlow<Set<String>> = _selectedItemIds

    // Per modificar el set des de fora
    fun setSelectedItemIds(ids: Set<String>) {
        _selectedItemIds.value = ids
    }

    fun markItem(itemId: String, checked: Boolean) {
        _selectedItemIds.value = if (checked) _selectedItemIds.value + itemId else _selectedItemIds.value - itemId
    }

    fun resetSession() {
        _selectedItemIds.value = emptySet()
    }
}