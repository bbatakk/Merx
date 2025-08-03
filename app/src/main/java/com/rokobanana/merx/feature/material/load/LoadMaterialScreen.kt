package com.rokobanana.merx.feature.material.load

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rokobanana.merx.feature.autenticacio.AuthViewModel

/**
 * Main wrapper screen that handles navigation between material selection and checklist
 */
@Composable
fun LoadMaterialScreen(
    navController: NavController,
    grupId: String,
    grupNom: String,
    authViewModel: AuthViewModel,
    viewModel: LoadMaterialViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state.currentStep) {
        LoadStep.SELECT -> {
            LoadMaterialSelectScreen(
                state = state,
                onToggleMaterial = viewModel::toggleMaterialSelection,
                onProceedToChecklist = viewModel::proceedToChecklist,
                navController = navController,
                grupId = grupId,
                grupNom = grupNom,
                authViewModel = authViewModel
            )
        }
        LoadStep.CHECKLIST -> {
            LoadMaterialChecklistScreen(
                state = state,
                onToggleChecked = viewModel::toggleItemChecked,
                onGoBack = viewModel::goBackToSelection,
                onReset = viewModel::resetLoad,
                navController = navController,
                grupId = grupId,
                grupNom = grupNom,
                authViewModel = authViewModel
            )
        }
    }
}