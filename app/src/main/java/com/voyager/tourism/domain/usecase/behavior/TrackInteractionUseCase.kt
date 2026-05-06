package com.voyager.tourism.domain.usecase.behavior

import com.voyager.tourism.data.dto.BehaviorAnalysisSimpleResponse
import com.voyager.tourism.data.dto.InteractionType
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import javax.inject.Inject

/**
 * Use case for tracking user interactions for behavior analysis
 * Encapsulates business logic for interaction tracking
 */
class TrackInteractionUseCase @Inject constructor(
    private val behaviorAnalysisRepository: BehaviorAnalysisRepository
) {
    /**
     * Records a user interaction for behavior tracking and analysis.
     *
     * @param userId Authenticated user identifier.
     * @param interactionType Kind of interaction (for example view or click).
     * @param activityId Optional related activity or catalog id.
     * @param activityCategory Optional semantic category for grouping.
     * @param sessionDuration Optional session length in seconds.
     * @param context Arbitrary context map; a timestamp entry is added when absent.
     * @return Result from the behavior analysis API.
     */
    suspend operator fun invoke(
        userId: String,
        interactionType: InteractionType,
        activityId: String? = null,
        activityCategory: String? = null,
        sessionDuration: Int? = null,
        context: Map<String, Any> = emptyMap()
    ): Result<BehaviorAnalysisSimpleResponse> {
        // Validate input
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }
        
        // Add timestamp to context if not present
        val enrichedContext = context.toMutableMap()
        if (!enrichedContext.containsKey("timestamp")) {
            enrichedContext["timestamp"] = System.currentTimeMillis()
        }
        
        return behaviorAnalysisRepository.trackInteraction(
            userId = userId,
            interactionType = interactionType,
            activityId = activityId,
            activityCategory = activityCategory,
            sessionDuration = sessionDuration,
            context = enrichedContext
        )
    }
}
