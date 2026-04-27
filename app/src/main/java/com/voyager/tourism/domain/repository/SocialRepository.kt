package com.voyager.tourism.domain.repository

import com.voyager.tourism.domain.model.CompatibilityMatch
import com.voyager.tourism.domain.model.SharedActivity
import com.voyager.tourism.domain.model.SharedActivityDecision
import com.voyager.tourism.domain.model.TravelActivity
import com.voyager.tourism.domain.model.TravelerConnection

interface SocialRepository {
    suspend fun getConnections(userId: Long): Result<List<TravelerConnection>>
    suspend fun getTravelPlanActivities(travelPlanId: Long): Result<List<TravelActivity>>
    suspend fun shareActivity(activityId: Long, receiverId: Long): Result<SharedActivity>
    suspend fun resolveSharedActivity(
        sharedActivityId: Long,
        decision: SharedActivityDecision
    ): Result<SharedActivity>

    suspend fun getCompatibilityMatches(
        destination: String,
        startDate: String,
        endDate: String,
        interests: List<String>
    ): Result<List<CompatibilityMatch>>
}
