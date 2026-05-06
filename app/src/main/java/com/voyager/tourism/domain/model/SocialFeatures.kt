package com.voyager.tourism.domain.model

data class TravelerConnection(
    val id: Long,
    val username: String,
    val status: String
)

data class TravelActivity(
    val id: Long,
    val name: String
)

enum class SharedActivityDecision {
    ACCEPT,
    REJECT
}

data class SharedActivity(
    val id: Long,
    val activityId: Long,
    val senderId: Long,
    val receiverId: Long,
    val status: String,
    val sharedPlan: Boolean
)

data class CompatibilityMatch(
    val userId: Long,
    val totalScore: Double,
    val destinationScore: Double,
    val dateProximityScore: Double,
    val interestScore: Double,
    val matchedInterests: List<String>
)
