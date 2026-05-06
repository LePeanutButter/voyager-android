package com.voyager.tourism.presentation.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.voyager.tourism.presentation.viewmodel.LoginViewModel
import com.voyager.tourism.presentation.viewmodel.LoginUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is LoginUiState.GoogleLoginInitiated -> {
                // Google login initiated, waiting for callback
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Tu asistente de viajes inteligente",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Username/Email field
        OutlinedTextField(
            value = usernameOrEmail,
            onValueChange = { 
                usernameOrEmail = it
                usernameError = if (it.isBlank()) "El usuario o correo es requerido" else null
            },
            label = { Text("Usuario o correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginUiState.Loading && uiState !is LoginUiState.GoogleLoginInitiated
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = if (it.isBlank()) "La contraseña es requerida" else null
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginUiState.Loading && uiState !is LoginUiState.GoogleLoginInitiated
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Show error message if login fails
        when (val state = uiState) {
            is LoginUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
            else -> {}
        }
        
        // Login button
        Button(
            onClick = {
                // Validate fields
                val hasError = validateLoginFields(usernameOrEmail, password) { field, error ->
                    when (field) {
                        "username" -> usernameError = error
                        "password" -> passwordError = error
                    }
                }
                
                if (!hasError) {
                    viewModel.login(usernameOrEmail, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginUiState.Loading && uiState !is LoginUiState.GoogleLoginInitiated
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar sesión")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google login button
        OutlinedButton(
            onClick = { viewModel.loginWithGoogle(context) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginUiState.Loading && uiState !is LoginUiState.GoogleLoginInitiated
        ) {
            // Google icon (you can add a proper Google icon here)
            Text("Continuar con Google")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Register link
        TextButton(
            onClick = { navController.navigate("register") },
            enabled = uiState !is LoginUiState.Loading && uiState !is LoginUiState.GoogleLoginInitiated
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

private fun validateLoginFields(
    usernameOrEmail: String,
    password: String,
    onError: (field: String, error: String) -> Unit
): Boolean {
    var hasError = false
    
    if (usernameOrEmail.isBlank()) {
        onError("username", "El usuario o correo es requerido")
        hasError = true
    }
    
    if (password.isBlank()) {
        onError("password", "La contraseña es requerida")
        hasError = true
    }
    
    return hasError
}
