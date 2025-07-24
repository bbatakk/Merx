package com.rokobanana.merx.feature.afegirProducte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductesViewModelFactory(private val grupId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductesViewModel(grupId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}