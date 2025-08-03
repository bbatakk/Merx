package com.rokobanana.merx.feature.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rokobanana.merx.feature.autenticacio.AuthViewModel

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDrawer(
    grupNom: String,
    menuNom: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    grupId: String
) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxHeight()
            .width(LocalConfiguration.current.screenWidthDp.dp * 2 / 3)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Menú  |  $grupNom",
            style = MaterialTheme.typography.titleMedium,color = MaterialTheme.colorScheme.onBackground
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )
        NavigationDrawerItem(
            label = { Text("Perfil") },
            selected = false,
            onClick = { navController.navigate("perfil") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") }
        )
        NavigationDrawerItem(
            label = { Text("Grups") },
            selected = false,
            onClick = {
                navController.navigate("menuGrups") {
                    popUpTo("llistaProductes/$grupId") { inclusive = true }
                }
            },
            icon = { Icon(Icons.Default.Group, contentDescription = "Grups") }
        )
        NavigationDrawerItem(
            label = { Text("Tancar sessió") },
            selected = false,
            onClick = {
                authViewModel.signOut()
                navController.navigate("login") {
                    popUpTo("seleccio") { inclusive = true }
                }
            },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Tancar sessió") }
        )
        // Pots afegir més opcions si vols!
    }
}