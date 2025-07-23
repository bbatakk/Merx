package com.rokobanana.merx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.rokobanana.merx.ui.afegirProducte.AfegirProducteScreen
import com.rokobanana.merx.ui.editarProducte.DetallProducteScreen
import com.rokobanana.merx.ui.llistaProducte.LlistaProductesScreen
import com.rokobanana.merx.ui.theme.MerxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            MerxTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "llista"
                ) {
                    composable("llista") {
                        LlistaProductesScreen(navController = navController)
                    }
                    composable("nou") {
                        AfegirProducteScreen(navController = navController)
                    }
                    composable("detall/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        DetallProducteScreen(producteId = id, navController = navController)
                    }
                }
            }
        }
    }
}
