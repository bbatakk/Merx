package com.rokobanana.merx.feature.material.load

import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.test.runTest

/**
 * Simple unit test for LoadMaterialViewModel to validate the core functionality
 */
class LoadMaterialViewModelTest {

    @Test
    fun `initial state should be SELECT step with empty selections`() = runTest {
        val viewModel = LoadMaterialViewModel()
        val state = viewModel.state.value
        
        assertEquals(LoadStep.SELECT, state.currentStep)
        assertTrue(state.selectedMaterials.isEmpty())
        assertTrue(state.checkedItems.isEmpty())
        assertFalse(state.isCompleted)
        assertTrue(state.availableMaterials.isNotEmpty()) // Dummy data should be loaded
    }

    @Test
    fun `toggle material selection should add and remove materials`() = runTest {
        val viewModel = LoadMaterialViewModel()
        
        // Initially no materials selected
        assertTrue(viewModel.state.value.selectedMaterials.isEmpty())
        
        // Select first material
        val firstMaterialId = viewModel.state.value.availableMaterials.first().id
        viewModel.toggleMaterialSelection(firstMaterialId)
        
        // Should have one material selected
        assertEquals(1, viewModel.state.value.selectedMaterials.size)
        assertTrue(viewModel.state.value.selectedMaterials.any { it.id == firstMaterialId })
        
        // Deselect the same material
        viewModel.toggleMaterialSelection(firstMaterialId)
        
        // Should have no materials selected
        assertTrue(viewModel.state.value.selectedMaterials.isEmpty())
    }

    @Test
    fun `proceed to checklist should change step when materials are selected`() = runTest {
        val viewModel = LoadMaterialViewModel()
        
        // Select a material first
        val firstMaterialId = viewModel.state.value.availableMaterials.first().id
        viewModel.toggleMaterialSelection(firstMaterialId)
        
        // Proceed to checklist
        viewModel.proceedToChecklist()
        
        // Should be in checklist step
        assertEquals(LoadStep.CHECKLIST, viewModel.state.value.currentStep)
    }

    @Test
    fun `checklist completion should mark as completed when all items checked`() = runTest {
        val viewModel = LoadMaterialViewModel()
        
        // Select two materials
        val materials = viewModel.state.value.availableMaterials.take(2)
        materials.forEach { viewModel.toggleMaterialSelection(it.id) }
        
        // Go to checklist
        viewModel.proceedToChecklist()
        
        // Check first item
        viewModel.toggleItemChecked(materials[0].id)
        assertFalse(viewModel.state.value.isCompleted)
        
        // Check second item
        viewModel.toggleItemChecked(materials[1].id)
        assertTrue(viewModel.state.value.isCompleted)
    }

    @Test
    fun `go back to selection should reset checklist state`() = runTest {
        val viewModel = LoadMaterialViewModel()
        
        // Select material and go to checklist
        val materialId = viewModel.state.value.availableMaterials.first().id
        viewModel.toggleMaterialSelection(materialId)
        viewModel.proceedToChecklist()
        
        // Check the item
        viewModel.toggleItemChecked(materialId)
        
        // Go back to selection
        viewModel.goBackToSelection()
        
        // Should be back in SELECT step with checklist state reset
        assertEquals(LoadStep.SELECT, viewModel.state.value.currentStep)
        assertTrue(viewModel.state.value.checkedItems.isEmpty())
        assertFalse(viewModel.state.value.isCompleted)
        // But selected materials should be preserved
        assertFalse(viewModel.state.value.selectedMaterials.isEmpty())
    }

    @Test
    fun `reset load should clear all state except available materials`() = runTest {
        val viewModel = LoadMaterialViewModel()
        val originalMaterials = viewModel.state.value.availableMaterials
        
        // Select materials and go to checklist
        val materialId = originalMaterials.first().id
        viewModel.toggleMaterialSelection(materialId)
        viewModel.proceedToChecklist()
        viewModel.toggleItemChecked(materialId)
        
        // Reset
        viewModel.resetLoad()
        
        // Should be back to initial state
        assertEquals(LoadStep.SELECT, viewModel.state.value.currentStep)
        assertTrue(viewModel.state.value.selectedMaterials.isEmpty())
        assertTrue(viewModel.state.value.checkedItems.isEmpty())
        assertFalse(viewModel.state.value.isCompleted)
        assertEquals(originalMaterials, viewModel.state.value.availableMaterials)
    }
}