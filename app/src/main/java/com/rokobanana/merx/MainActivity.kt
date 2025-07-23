package com.rokobanana.merx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.rokobanana.merx.ui.afegirProducte.AfegirProducteScreen
import com.rokobanana.merx.ui.editarProducte.DetallProducteScreen
import com.rokobanana.merx.ui.llistaProducte.LlistaProductesScreen
import com.rokobanana.merx.ui.seleccionarGrup.SeleccionarGrupScreen
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
                    startDestination = "seleccio"
                ) {
                    composable("seleccio") {
                        SeleccionarGrupScreen(navController = navController)
                    }

                    composable(
                        route = "llista/{grupId}",
                        arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                        LlistaProductesScreen(navController = navController, grupId = grupId)
                    }

                    composable(
                        route = "nou/{grupId}",
                        arguments = listOf(navArgument("grupId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                        AfegirProducteScreen(navController = navController, grupId = grupId)
                    }

                    composable(
                        route = "detall/{grupId}/{id}",
                        arguments = listOf(
                            navArgument("grupId") { type = NavType.StringType },
                            navArgument("id") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        DetallProducteScreen(grupId = grupId, producteId = id, navController = navController)
                    }

                }
            }
        }
    }
}
