package com.voyager.tourism.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voyager.tourism.presentation.viewmodel.AuthViewModel

/**
 * User profile screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            currentUser?.let { user ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // User Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.AccountCircle,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column {
                                        Text(
                                            text = "${user.firstName} ${user.lastName}",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = user.username,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = user.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        // Preferences Card
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Travel Preferences",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                user.preferences?.let { preferences ->
                                    PreferenceItem(
                                        title = "Travel Style",
                                        value = preferences.travelStyle.name
                                    )
                                    PreferenceItem(
                                        title = "Budget Range",
                                        value = preferences.budgetRange.name
                                    )
                                    PreferenceItem(
                                        title = "Accommodation",
                                        value = preferences.accommodationType.name
                                    )
                                    
                                    if (preferences.interests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Interests:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = preferences.interests.joinToString(", "),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                } ?: run {
                                    Text(
                                        text = "No preferences set",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        // Actions Card
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Actions",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedButton(
                                    onClick = { /* Edit profile */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Edit Profile")
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedButton(
                                    onClick = { /* Settings */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Settings")
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Button(
                                    onClick = onLogout,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Logout")
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No user data available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
