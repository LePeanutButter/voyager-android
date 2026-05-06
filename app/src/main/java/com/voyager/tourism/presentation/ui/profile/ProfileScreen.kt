package com.voyager.tourism.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.presentation.viewmodel.ProfileViewModel
import com.voyager.tourism.presentation.viewmodel.ProfileUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Profile screen for editing user information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit = {},
    onTravelPreferences: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Form fields state
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf(listOf<String>()) }
    var newInterest by remember { mutableStateOf("") }
    
    // Error states
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var bioError by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState(0)
    val coroutineScope = rememberCoroutineScope()
    
    // Load profile data when screen is first displayed
    LaunchedEffect(Unit) {
        val userId = viewModel.getCurrentUserId()
        if (userId != null) {
            viewModel.loadProfile(userId)
        }
    }
    
    // Update form fields when user data is loaded
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ProfileUiState.Loaded -> {
                val user = state.user
                firstName = user.firstName
                lastName = user.lastName
                bio = user.bio ?: ""
                phoneNumber = user.phoneNumber ?: ""
                interests = user.interests?.toList() ?: emptyList() // Convert Set to List for UI
            }
            else -> {}
        }
    }
    
    // Show success message
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.SaveSuccess) {
            // Show snackbar or toast message
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Completar Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar")
            }
        }
        
        when (uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Loaded, is ProfileUiState.SaveSuccess -> {
                // First Name field
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { 
                        firstName = it
                        firstNameError = if (it.isBlank()) "El nombre es requerido" else null
                    },
                    label = { Text("Nombre *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = firstNameError != null,
                    supportingText = firstNameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is ProfileUiState.Saving
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Last Name field
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { 
                        lastName = it
                        lastNameError = if (it.isBlank()) "El apellido es requerido" else null
                    },
                    label = { Text("Apellido *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = lastNameError != null,
                    supportingText = lastNameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is ProfileUiState.Saving
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bio field
                OutlinedTextField(
                    value = bio,
                    onValueChange = { 
                        bio = it
                        bioError = if (it.length > 500) "La biografía no puede exceder 500 caracteres" else null
                    },
                    label = { Text("Biografía corta *") },
                    supportingText = { 
                        Text(
                            text = "${bio.length}/500 caracteres",
                            color = if (bio.length > 500) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = bioError != null,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = uiState !is ProfileUiState.Saving
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone number field
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Teléfono (opcional)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is ProfileUiState.Saving
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Interests section
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Intereses",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Add new interest field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newInterest,
                                onValueChange = { newInterest = it },
                                label = { Text("Agregar interés") },
                                modifier = Modifier.weight(1f),
                                enabled = uiState !is ProfileUiState.Saving
                            )
                            
                            IconButton(
                                onClick = {
                                    if (newInterest.isNotBlank() && !interests.contains(newInterest)) {
                                        interests = interests + newInterest
                                        newInterest = ""
                                    }
                                },
                                enabled = uiState !is ProfileUiState.Saving
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar interés")
                            }
                        }
                        
                        // Display interests as chips
                        if (interests.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            InterestChips(
                                interests = interests,
                                onRemoveInterest = { 
                                    interests = interests - it
                                },
                                enabled = uiState !is ProfileUiState.Saving
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Save button
                Button(
                    onClick = {
                        // Validate required fields
                        val hasError = validateProfileFields(firstName, lastName, bio) { field, error ->
                            when (field) {
                                "firstName" -> firstNameError = error
                                "lastName" -> lastNameError = error
                                "bio" -> bioError = error
                            }
                        }
                        
                        if (!hasError) {
                            val userId = viewModel.getCurrentUserId()
                            if (userId != null) {
                                viewModel.saveProfile(
                                    userId = userId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    bio = bio,
                                    phoneNumber = phoneNumber.ifBlank { null },
                                    interests = interests
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is ProfileUiState.Saving
                ) {
                    if (uiState is ProfileUiState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar perfil")
                    }
                }
            }
            
            is ProfileUiState.Error -> {
                Text(
                    text = (uiState as ProfileUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            is ProfileUiState.Saving -> {
                // Show loading state while saving
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InterestChips(
    interests: List<String>,
    onRemoveInterest: (String) -> Unit,
    enabled: Boolean
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(interests) { interest ->
            FilterChip(
                selected = true,
                onClick = { onRemoveInterest(interest) },
                label = { Text(interest) },
                enabled = enabled
            )
        }
    }
}

private fun validateProfileFields(
    firstName: String,
    lastName: String,
    bio: String,
    onError: (field: String, error: String) -> Unit
): Boolean {
    var hasError = false
    
    if (firstName.isBlank()) {
        onError("firstName", "El nombre es requerido")
        hasError = true
    }
    
    if (lastName.isBlank()) {
        onError("lastName", "El apellido es requerido")
        hasError = true
    }
    
    if (bio.length > 500) {
        onError("bio", "La biografía no puede exceder 500 caracteres")
        hasError = true
    }
    
    return hasError
}
