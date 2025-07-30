package com.rokobanana.merx.ui.autenticacio

<<<<<<< Updated upstream:app/src/main/java/com/rokobanana/merx/ui/autenticacio/LoginScreen.kt
import android.app.Application
=======
import androidx.activity.compose.BackHandler
>>>>>>> Stashed changes:app/src/main/java/com/rokobanana/merx/feature/autenticacio/ui/LoginScreen.kt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
<<<<<<< Updated upstream:app/src/main/java/com/rokobanana/merx/ui/autenticacio/LoginScreen.kt
import androidx.lifecycle.viewmodel.compose.viewModel
=======
import androidx.hilt.navigation.compose.hiltViewModel
import com.rokobanana.merx.feature.autenticacio.AuthViewModel
>>>>>>> Stashed changes:app/src/main/java/com/rokobanana/merx/feature/autenticacio/ui/LoginScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val user by authViewModel.userState.collectAsState()

    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Iniciar sessió", style = MaterialTheme.typography.headlineMedium) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = { Text("Correu electrònic") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                label = { Text("Contrasenya") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { authViewModel.login(emailState.value.trim(), passwordState.value.trim()) },
                enabled = emailState.value.isNotBlank() && passwordState.value.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }
            Spacer(Modifier.height(8.dp))
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            TextButton(onClick = onNavigateToRegister) {
                Text("Registrar-se", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
