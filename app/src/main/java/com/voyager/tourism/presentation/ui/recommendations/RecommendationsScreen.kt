package com.voyager.tourism.presentation.ui.recommendations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Screen showing AI-powered destination recommendations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    onPlaceClick: (String) -> Unit
) {
    // Placeholder data - in real app, this would come from ViewModel
    val recommendations = remember {
        listOf(
            RecommendationItem(
                id = "1",
                name = "Paris, France",
                description = "City of lights with amazing cuisine and art",
                rating = 4.8f,
                price = "$$$",
                category = "Cultural"
            ),
            RecommendationItem(
                id = "2", 
                name = "Bali, Indonesia",
                description = "Tropical paradise with beautiful beaches",
                rating = 4.6f,
                price = "$$",
                category = "Beach"
            ),
            RecommendationItem(
                id = "3",
                name = "Tokyo, Japan",
                description = "Modern metropolis with rich traditions",
                rating = 4.9f,
                price = "$$$$",
                category = "Urban"
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "AI Recommendations",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Personalized destinations based on your preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(recommendations) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    onClick = { onPlaceClick(recommendation.id) }
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: RecommendationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = recommendation.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = recommendation.price,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recommendation.rating.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = recommendation.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

data class RecommendationItem(
    val id: String,
    val name: String,
    val description: String,
    val rating: Float,
    val price: String,
    val category: String
)
