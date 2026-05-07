package com.voyager.tourism.presentation.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.voyager.tourism.presentation.viewmodel.RegisterViewModel
import com.voyager.tourism.presentation.viewmodel.RegisterUiState

private const val MSG_CONFIRM_MISMATCH = "Las contraseñas no coinciden"

/**
 * Account registration screen collecting profile fields and submitting them to the backend.
 *
 * @param navController Used to return to the login route after successful signup.
 * @param viewModel Owns registration state and API call.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("kotlin:S3776")
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterUiState.Success -> {
                // Show success message and navigate to login
                android.widget.Toast.makeText(
                    context,
                    "Cuenta creada exitosamente",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }
            is RegisterUiState.Error -> {
                // Error is handled in the UI display
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
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                usernameError = if (it.isBlank()) "El nombre de usuario es requerido" else null
            },
            label = { Text("Nombre de usuario") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            isError = usernameError != null,
            supportingText = usernameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                emailError = when {
                    it.isBlank() -> "El correo electrónico es requerido"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() -> "Formato de correo inválido"
                    else -> null
                }
            },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // First Name field
        OutlinedTextField(
            value = firstName,
            onValueChange = { 
                firstName = it
                firstNameError = if (it.isBlank()) "El nombre es requerido" else null
            },
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            isError = firstNameError != null,
            supportingText = firstNameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Last Name field
        OutlinedTextField(
            value = lastName,
            onValueChange = { 
                lastName = it
                lastNameError = if (it.isBlank()) "El apellido es requerido" else null
            },
            label = { Text("Apellido") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            isError = lastNameError != null,
            supportingText = lastNameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = when {
                    it.isBlank() -> "La contraseña es requerida"
                    it.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
                    else -> null
                }
                // Revalidate confirm password if it has content
                if (confirmPassword.isNotBlank()) {
                    confirmPasswordError = if (it != confirmPassword) {
                        MSG_CONFIRM_MISMATCH
                    } else null
                }
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
            enabled = uiState !is RegisterUiState.Loading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                confirmPasswordError = when {
                    it.isBlank() -> "Confirmar la contraseña es requerido"
                    it != password -> MSG_CONFIRM_MISMATCH
                    else -> null
                }
            },
            label = { Text("Confirmar contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Register button
        Button(
            onClick = {
                // Validate all fields
                val hasError = validateFields(
                    username, email, password, confirmPassword, firstName, lastName
                ) { field, error ->
                    when (field) {
                        "username" -> usernameError = error
                        "email" -> emailError = error
                        "password" -> passwordError = error
                        "confirmPassword" -> confirmPasswordError = error
                        "firstName" -> firstNameError = error
                        "lastName" -> lastNameError = error
                    }
                }
                
                if (!hasError) {
                    viewModel.register(username, email, password, firstName, lastName)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterUiState.Loading
        ) {
            if (uiState is RegisterUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Registrarse")
            }
        }
        
        // Show error message if registration fails
        when (val state = uiState) {
            is RegisterUiState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {}
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Login link
        TextButton(
            onClick = { navController.navigate("login") },
            enabled = uiState !is RegisterUiState.Loading
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}

/**
 * Validates all registration fields (username, email, passwords, names).
 *
 * @param onError Invoked with a field key and message for the first invalid value per field batch.
 * @return `true` if any field is invalid, `false` if the form may be submitted.
 */
private fun validateFields(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    firstName: String,
    lastName: String,
    onError: (field: String, error: String) -> Unit
): Boolean {
    var hasError = false
    
    if (username.isBlank()) {
        onError("username", "El nombre de usuario es requerido")
        hasError = true
    }
    
    if (email.isBlank()) {
        onError("email", "El correo electrónico es requerido")
        hasError = true
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onError("email", "Formato de correo inválido")
        hasError = true
    }
    
    if (password.isBlank()) {
        onError("password", "La contraseña es requerida")
        hasError = true
    } else if (password.length < 8) {
        onError("password", "La contraseña debe tener al menos 8 caracteres")
        hasError = true
    }
    
    if (confirmPassword.isBlank()) {
        onError("confirmPassword", "Confirmar la contraseña es requerido")
        hasError = true
    } else if (password != confirmPassword) {
        onError("confirmPassword", MSG_CONFIRM_MISMATCH)
        hasError = true
    }
    
    if (firstName.isBlank()) {
        onError("firstName", "El nombre es requerido")
        hasError = true
    }
    
    if (lastName.isBlank()) {
        onError("lastName", "El apellido es requerido")
        hasError = true
    }
    
    return hasError
}
