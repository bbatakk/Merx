package com.rokobanana.merx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rokobanana.merx.feature.afegirProducte.ui.AfegirProducteScreen
import com.rokobanana.merx.feature.autenticacio.ui.LoginScreen
import com.rokobanana.merx.feature.autenticacio.ui.RegisterScreen
import com.rokobanana.merx.feature.editarProducte.DetallProducteScreen
import com.rokobanana.merx.feature.llistaProducte.LlistaProductesScreen
import com.rokobanana.merx.feature.seleccionarGrup.LlistaGrupsScreen
import com.rokobanana.merx.feature.seleccionarGrup.MenuGrupsScreen
import com.rokobanana.merx.feature.seleccionarGrup.TriarGrupScreen
import com.rokobanana.merx.theme.MerxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            MerxTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }
                var pendingGrupSelection by remember { mutableStateOf(false) }
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        startDestination = "login"
                    } else {
                        startDestination = "menuGrups"
                    }
                }

                if (pendingGrupSelection) {
                    TriarGrupScreen(navController = navController) { selectedGrupId ->
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("usuaris").document(uid).update("grupActual", selectedGrupId)
                                .addOnSuccessListener {
                                    navController.navigate("llista/$selectedGrupId") {
                                        popUpTo(0)
                                    }
                                }
                        }
                    }
                } else if (startDestination != null) {
                    NavHost(navController = navController, startDestination = startDestination!!) {
                        composable("checkGroups") {
                            LaunchedEffect(Unit) {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user != null) {
                                    val db = FirebaseFirestore.getInstance()
                                    db.collection("usuaris").document(user.uid).get()
                                        .addOnSuccessListener { doc ->
                                            val grups = (doc.get("grups") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                            val grupActual = doc.getString("grupActual")
                                            when {
                                                grups.isEmpty() -> navController.navigate("seleccio") {
                                                    popUpTo("checkGroups") { inclusive = true }
                                                }
                                                grups.size == 1 -> navController.navigate("llista/${grups.first()}") {
                                                    popUpTo("checkGroups") { inclusive = true }
                                                }
                                                !grupActual.isNullOrEmpty() -> navController.navigate("llista/$grupActual") {
                                                    popUpTo("checkGroups") { inclusive = true }
                                                }
                                                else -> navController.navigate("triarGrup") {
                                                    popUpTo("checkGroups") { inclusive = true }
                                                }
                                            }
                                        }
                                }
                            }
                            // Opcional: loader mentre fa la comprovació
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("menuGrups") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(onRegisterSuccess = {
                                navController.navigate("menuGrups") {
                                    popUpTo("register") { inclusive = true }
                                }
                            })
                        }
                        composable("menuGrups") {
                            MenuGrupsScreen(navController = navController)
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
                            route = "detall/{grupId}/{producteId}",
                            arguments = listOf(
                                navArgument("grupId") { type = NavType.StringType },
                                navArgument("producteId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val grupId = backStackEntry.arguments?.getString("grupId") ?: ""
                            val producteId = backStackEntry.arguments?.getString("producteId") ?: ""
                            DetallProducteScreen(
                                navController = navController,
                                grupId = grupId,
                                producteId = producteId
                            )
                        }
                        composable("grups") {
                            LlistaGrupsScreen(navController = navController, userId = FirebaseAuth.getInstance().currentUser?.uid ?: "")
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
