package com.rokobanana.merx.ui.afegirProducte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductesViewModelFactory(private val grupId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductesViewModel(grupId) as T
    }
}