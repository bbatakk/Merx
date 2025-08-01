package com.rokobanana.merx.feature.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.repository.MaterialSetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialSetsViewModel @Inject constructor(
    private val repository: MaterialSetRepository
) : ViewModel() {

    private val _sets = MutableStateFlow<List<MaterialSet>>(emptyList())
    val sets: StateFlow<List<MaterialSet>> = _sets

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentGrupId: String? = null

    fun loadSets(grupId: String) {
        currentGrupId = grupId
        repository.getAllSets(grupId)
            .onEach { _sets.value = it }
            .catch { _error.value = it.message }
            .launchIn(viewModelScope)
    }

    fun addSet(set: MaterialSet) {
        val grupId = currentGrupId ?: return
        _loading.value = true
        viewModelScope.launch {
            try {
                repository.addSet(grupId, set)
                _error.value = null
                loadSets(grupId) // refresca la llista!
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateSet(set: MaterialSet) {
        val grupId = currentGrupId ?: return
        _loading.value = true
        viewModelScope.launch {
            try {
                repository.updateSet(grupId, set)
                _error.value = null
                loadSets(grupId) // refresca la llista!
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteSet(setId: String) {
        val grupId = currentGrupId ?: return
        _loading.value = true
        viewModelScope.launch {
            try {
                repository.deleteSet(grupId, setId)
                _error.value = null
                loadSets(grupId) // refresca la llista!
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}