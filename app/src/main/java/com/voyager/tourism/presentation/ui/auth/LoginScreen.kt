package com.voyager.tourism.presentation.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.voyager.tourism.presentation.ui.theme.SmarTripColors
import com.voyager.tourism.presentation.ui.theme.SmarTripLogo
import com.voyager.tourism.presentation.ui.theme.SmarTripLogoVariant
import com.voyager.tourism.presentation.viewmodel.AuthViewModel
import com.voyager.tourism.presentation.viewmodel.LoginUiState
import com.voyager.tourism.presentation.viewmodel.LoginViewModel

/**
 * Equivalente a [voyager-web-client/src/pages/Auth/LoginPage.jsx]: panel hero + formulario,
 * validaciones y Google OAuth.
 */
@Composable
@Suppress("kotlin:S3776")
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Igual que el flujo clásico onActivityResult: si hay Intent con datos, se resuelve la Task;
    // no depender solo de RESULT_OK (en algunos dispositivos el código no coincide y nunca se llama al VM).
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data == null) {
            viewModel.resetState()
            return@rememberLauncherForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        viewModel.handleGoogleSignInResult(task)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                val user = (uiState as LoginUiState.Success).user
                authViewModel.adoptAuthenticatedUser(user)
                navController.navigate(AuthViewModel.ROUTE_DASHBOARD) {
                    launchSingleTop = true
                    popUpTo(AuthViewModel.ROUTE_LOGIN) { inclusive = true }
                }
            }
            is LoginUiState.GoogleLoginInitiated -> Unit
            else -> Unit
        }
    }

    val fieldShape = RoundedCornerShape(14.dp)
    val loading = uiState is LoginUiState.Loading || uiState is LoginUiState.GoogleLoginInitiated

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            SmarTripColors.Gray50,
                            Color(0xFFEEF7FF),
                        ),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 28.dp),
        ) {
            Column {
                SmarTripLogo(
                    variant = SmarTripLogoVariant.OnLightBackground,
                    modifier = Modifier
                        .height(44.dp)
                        .fillMaxWidth(0.55f),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Your journey starts here",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SmarTripColors.BrandInk,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Plan smarter trips, connect with fellow travelers, and let AI guide your adventures.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SmarTripColors.Gray600,
                )
                Spacer(modifier = Modifier.height(16.dp))
                val features = listOf(
                    "AI-powered travel planning",
                    "Real-time traveler matching",
                    "Smart itinerary builder",
                )
                features.forEach { line ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SmarTripColors.BrandCyan, RoundedCornerShape(4.dp)),
                        )
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = SmarTripColors.Gray600,
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Bienvenido de nuevo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Inicia sesion en tu cuenta de SmarTrip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = usernameOrEmail,
                    onValueChange = {
                        usernameOrEmail = it
                        usernameError = if (it.isBlank()) "Username or email is required" else null
                    },
                    label = { Text("Username or Email") },
                    placeholder = { Text("your_username or email@example.com") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = usernameError != null,
                    supportingText = usernameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SmarTripColors.BrandBlueDark,
                        focusedLabelColor = SmarTripColors.BrandBlueDark,
                        cursorColor = SmarTripColors.BrandBlueDark,
                    ),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = if (it.isBlank()) "Password is required" else null
                    },
                    label = { Text("Password") },
                    placeholder = { Text("••••••••") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SmarTripColors.BrandBlueDark,
                        focusedLabelColor = SmarTripColors.BrandBlueDark,
                        cursorColor = SmarTripColors.BrandBlueDark,
                    ),
                    singleLine = true,
                )

                if (uiState is LoginUiState.Error) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = (uiState as LoginUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val gradient = Brush.horizontalGradient(
                    colors = listOf(SmarTripColors.GradientPrimaryStart, SmarTripColors.GradientPrimaryEnd),
                )
                Button(
                    onClick = {
                        val hasError = validateLoginFields(usernameOrEmail, password) { field, error ->
                            when (field) {
                                "username" -> usernameError = error
                                "password" -> passwordError = error
                            }
                        }
                        if (!hasError) {
                            viewModel.login(usernameOrEmail.trim(), password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !loading,
                    shape = fieldShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradient, fieldShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                                Text("Signing in…", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Text("Sign In", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Text(
                        text = "or",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { 
                        val signInIntent = viewModel.getGoogleSignInIntent(context)
                        googleSignInLauncher.launch(signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    shape = fieldShape,
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuar con Google")
                }

                Spacer(modifier = Modifier.height(20.dp))
                TextButton(
                    onClick = { navController.navigate(AuthViewModel.ROUTE_REGISTER) },
                    enabled = !loading,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("¿No tienes cuenta? Regístrate")
                }
            }
        }
    }
}

private fun validateLoginFields(
    usernameOrEmail: String,
    password: String,
    onError: (field: String, error: String) -> Unit,
): Boolean {
    var hasError = false
    if (usernameOrEmail.isBlank()) {
        onError("username", "Username or email is required")
        hasError = true
    }
    if (password.isBlank()) {
        onError("password", "Password is required")
        hasError = true
    }
    return hasError
}
