package com.voyager.tourism.presentation.ui.social

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.presentation.viewmodel.TravelerMatchingViewModel
import com.voyager.tourism.util.formatBackendLocalDateTime

/**
 * Screen for finding and connecting with compatible travelers
 *
 * This screen implements Task 1: Search travelers by destination and similar dates
 * and Task 2: Send connection requests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("kotlin:S3776")
fun TravelerMatchingScreen(
    travelPlanId: String,
    token: String,
    onNavigateBack: () -> Unit,
    viewModel: TravelerMatchingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(travelPlanId) {
        viewModel.setCurrentTravelPlanId(travelPlanId)
        viewModel.findCompatibleTravelers(travelPlanId, token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        SocialScreenHeader(
            title = "Compatible Travelers",
            subtitle = "Travelers with similar destinations and compatible dates",
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
            SocialLoadingPlaceholder(caption = "Finding compatible travelers...")
        } else if (uiState.compatibleTravelers.isEmpty()) {
            SocialEmptyState(
                emoji = "🔍",
                title = "No Compatible Travelers Found",
                body = "No compatible travelers found for this travel plan.\nTry adjusting your travel dates or destination to find more matches.",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(uiState.compatibleTravelers) { traveler ->
                    TravelerCard(
                        traveler = traveler,
                        onSendRequest = { recipientId, _ ->
                            viewModel.sendConnectionRequest(recipientId, null, token)
                        },
                        isSending = uiState.isSendingRequest,
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelerCardHeaderRow(traveler: TravelerMatchDto) {
    val initials = "${traveler.firstName.firstOrNull() ?: '?'}${traveler.lastName.firstOrNull() ?: '?'}"
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
            SocialProfileAvatar(traveler.profileImageUrl, 60.dp, initials)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${traveler.firstName} ${traveler.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "@${traveler.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                traveler.bio?.let { bio ->
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${traveler.compatibilityScore ?: 0}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Match",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TravelerCardTripSummary(traveler: TravelerMatchDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = traveler.travelPlanTitle ?: "",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Destination: ${traveler.destinationLocation ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Travel dates: ${formatBackendLocalDateTime(traveler.travelStartDate, "MMM d, yyyy")} - ${formatBackendLocalDateTime(traveler.travelEndDate, "MMM d, yyyy")}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Overlap: ${traveler.daysOverlap} days",
                style = MaterialTheme.typography.bodySmall,
            )
            val n = traveler.numberOfTravelers ?: 0
            Text(
                text = "Group size: $n ${if (traveler.numberOfTravelers == 1) "traveler" else "travelers"}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Presents one compatible traveler, trip overlap context, and a connection CTA. */
@Composable
private fun TravelerCard(
    traveler: TravelerMatchDto,
    onSendRequest: (Long, String) -> Unit,
    isSending: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TravelerCardHeaderRow(traveler)
            Spacer(modifier = Modifier.height(16.dp))
            TravelerCardTripSummary(traveler)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSendRequest(traveler.userId, traveler.firstName) },
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sending...")
                } else {
                    Text("Send Connection Request")
                }
            }
        }
    }
}
