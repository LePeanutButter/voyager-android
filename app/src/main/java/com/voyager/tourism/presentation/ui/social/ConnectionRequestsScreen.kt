package com.voyager.tourism.presentation.ui.social

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.presentation.viewmodel.ConnectionRequestsViewModel

/**
 * Screen for managing connection requests
 *
 * This screen implements Task 3: Accept or reject connection requests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("kotlin:S3776")
fun ConnectionRequestsScreen(
    token: String,
    onNavigateBack: () -> Unit,
    viewModel: ConnectionRequestsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(token) {
        viewModel.loadPendingRequests(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        SocialScreenHeader(
            title = "Connection Requests",
            subtitle = "Manage requests from travelers who want to connect with you",
            onBack = onNavigateBack,
        )

        uiState.successMessage?.let { message ->
            SocialDismissibleBanner(
                message = message,
                kind = SocialBannerKind.Success,
                onDismiss = { viewModel.clearSuccessMessage() },
            )
        }

        uiState.error?.let { error ->
            SocialDismissibleBanner(
                message = error,
                kind = SocialBannerKind.Error,
                onDismiss = { viewModel.clearError() },
            )
        }

        if (uiState.isLoading) {
            SocialLoadingPlaceholder(caption = "Loading connection requests...")
        } else if (uiState.pendingRequests.isEmpty()) {
            SocialEmptyState(
                emoji = "📭",
                title = "No Pending Requests",
                body = "You don't have any pending connection requests at the moment.\nWhen travelers send you connection requests, they'll appear here.",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(uiState.pendingRequests) { request ->
                    ConnectionRequestCard(
                        request = request,
                        onAccept = { requestId -> viewModel.acceptRequest(requestId, token) },
                        onReject = { requestId -> viewModel.rejectRequest(requestId, token) },
                        isProcessing = uiState.isProcessing,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionRequestHeaderRow(request: ConnectionRequestDto) {
    val placeholderName = request.requesterName ?: "Traveler"
    val initial = placeholderName.first().toString()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SocialProfileAvatar(request.requesterProfileImage, 50.dp, initial)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = placeholderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Requested ${formatBackendLocalDateTime(request.createdAt, "MMM d, yyyy 'at' h:mm a")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Badge {
            Text(
                text = "Pending",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ConnectionRequestQuotedMessage(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Text(
            text = "\"$message\"",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionRequestActionRow(
    requestId: String,
    isCurrentlyProcessing: Boolean,
    isProcessing: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = { onAccept(requestId) },
            enabled = !isProcessing,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            shape = RoundedCornerShape(8.dp),
        ) {
            if (isCurrentlyProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Processing...")
            } else {
                Text("Accept")
            }
        }
        OutlinedButton(
            onClick = { onReject(requestId) },
            enabled = !isProcessing,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            if (isCurrentlyProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Processing...")
            } else {
                Text("Reject")
            }
        }
    }
}

@Composable
private fun ConnectionRequestCard(
    request: ConnectionRequestDto,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    isProcessing: Boolean,
) {
    val isCurrentlyProcessing = isProcessing && request.id.toString() in listOf("processing")
    val requestId = request.id.toString()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ConnectionRequestHeaderRow(request)
            Spacer(modifier = Modifier.height(12.dp))
            request.message?.let { message ->
                ConnectionRequestQuotedMessage(message)
                Spacer(modifier = Modifier.height(12.dp))
            }
            ConnectionRequestActionRow(
                requestId = requestId,
                isCurrentlyProcessing = isCurrentlyProcessing,
                isProcessing = isProcessing,
                onAccept = onAccept,
                onReject = onReject,
            )
        }
    }
}
