package com.rokobanana.merx.feature.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialItem
import com.rokobanana.merx.domain.model.MaterialSet
import com.rokobanana.merx.domain.repository.MaterialSetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditMaterialSetViewModel @Inject constructor(
    private val repository: MaterialSetRepository
) : ViewModel() {
    private val _set = MutableStateFlow(MaterialSet())
    val set: StateFlow<MaterialSet> = _set

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var grupId: String? = null

    // Ara carrega pel setId (o crea un set nou si no hi ha id)
    fun loadSet(grupId: String, setId: String?) {
        this.grupId = grupId
        viewModelScope.launch {
            _loading.value = true
            try {
                _set.value = if (!setId.isNullOrBlank()) {
                    repository.getSetById(grupId, setId)
                } else {
                    MaterialSet()
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateNom(nom: String) {
        _set.value = _set.value.copy(nom = nom)
    }

    fun addItem(nom: String, quantitat: Int = 1) {
        val items = _set.value.items + MaterialItem(id = UUID.randomUUID().toString(), nom = nom, quantitat = quantitat)
        _set.value = _set.value.copy(items = items)
    }

    fun updateItem(itemId: String, nom: String, quantitat: Int) {
        val items = _set.value.items.map {
            if (it.id == itemId) it.copy(nom = nom, quantitat = quantitat) else it
        }
        _set.value = _set.value.copy(items = items)
    }

    fun removeItem(itemId: String) {
        val items = _set.value.items.filterNot { it.id == itemId }
        _set.value = _set.value.copy(items = items)
    }

    fun saveSet(onFinish: (String) -> Unit) {
        val grupId = grupId ?: return
        _loading.value = true
        viewModelScope.launch {
            try {
                val setId = if (_set.value.id.isEmpty()) {
                    repository.addSet(grupId, _set.value)
                } else {
                    repository.updateSet(grupId, _set.value)
                    _set.value.id
                }
                onFinish(setId)
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }
}