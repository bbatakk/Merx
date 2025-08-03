package com.rokobanana.merx.feature.material.load

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rokobanana.merx.domain.model.MaterialItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class LoadMaterialState(
    val currentStep: LoadStep = LoadStep.SELECT,
    val availableMaterials: List<MaterialItem> = emptyList(),
    val selectedMaterials: List<MaterialItem> = emptyList(),
    val checkedItems: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false
)

enum class LoadStep {
    SELECT,
    CHECKLIST
}

data class MaterialWithSelection(
    val item: MaterialItem,
    val isSelected: Boolean = false
)

@HiltViewModel
class LoadMaterialViewModel @Inject constructor(
    // For MVP, we'll use a simple approach without external dependencies
    // In a real implementation, this would inject a use case to get all materials
) : ViewModel() {

    private val _state = MutableStateFlow(LoadMaterialState())
    val state: StateFlow<LoadMaterialState> = _state.asStateFlow()

    init {
        // For MVP, we'll use dummy data. In a real app, this would load from repository
        loadAvailableMaterials()
    }

    private fun loadAvailableMaterials() {
        // For MVP: dummy material data
        val dummyMaterials = listOf(
            MaterialItem(id = "1", nom = "Micròfon", marca = "Shure", model = "SM58", descripcio = "Micròfon dinàmic", quantitat = 2),
            MaterialItem(id = "2", nom = "Amplificador", marca = "Yamaha", model = "MG12XU", descripcio = "Amplificador de 12 canals", quantitat = 1),
            MaterialItem(id = "3", nom = "Cable XLR", marca = "Mogami", model = "2534", descripcio = "Cable de micròfon", quantitat = 5),
            MaterialItem(id = "4", nom = "Altaveu", marca = "JBL", model = "EON615", descripcio = "Altaveu actiu", quantitat = 2),
            MaterialItem(id = "5", nom = "Suport de micròfon", marca = "K&M", model = "210/2", descripcio = "Suport telescòpic", quantitat = 3)
        )
        
        _state.value = _state.value.copy(availableMaterials = dummyMaterials)
    }

    fun toggleMaterialSelection(materialId: String) {
        val currentState = _state.value
        val material = currentState.availableMaterials.find { it.id == materialId }
        
        if (material != null) {
            val updatedSelected = if (currentState.selectedMaterials.contains(material)) {
                currentState.selectedMaterials - material
            } else {
                currentState.selectedMaterials + material
            }
            
            _state.value = currentState.copy(selectedMaterials = updatedSelected)
        }
    }

    fun proceedToChecklist() {
        if (_state.value.selectedMaterials.isNotEmpty()) {
            _state.value = _state.value.copy(currentStep = LoadStep.CHECKLIST)
        }
    }

    fun goBackToSelection() {
        _state.value = _state.value.copy(
            currentStep = LoadStep.SELECT,
            checkedItems = emptySet(),
            isCompleted = false
        )
    }

    fun toggleItemChecked(materialId: String) {
        val currentState = _state.value
        val updatedChecked = if (currentState.checkedItems.contains(materialId)) {
            currentState.checkedItems - materialId
        } else {
            currentState.checkedItems + materialId
        }
        
        val isCompleted = updatedChecked.size == currentState.selectedMaterials.size
        
        _state.value = currentState.copy(
            checkedItems = updatedChecked,
            isCompleted = isCompleted
        )
    }

    fun resetLoad() {
        _state.value = LoadMaterialState(availableMaterials = _state.value.availableMaterials)
    }
}