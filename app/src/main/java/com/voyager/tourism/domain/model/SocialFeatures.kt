package com.voyager.tourism.domain.model

/**
 * Conexión aceptada: id de fila en backend ([TravelConnectionDto.id]) y datos del otro usuario ([userId] peer).
 */
data class TravelerConnection(
    val connectionId: Long,
    val peerUserId: Long,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val status: String,
)

/**
 * Catalog activity surfaced in social flows (for example sharing or matching).
 */
data class TravelActivity(
    val id: Long,
    val name: String
)

/**
 * User decision when responding to a shared-activity invitation.
 */
enum class SharedActivityDecision {
    ACCEPT,
    REJECT
}

/**
 * Record of an activity one traveler shared with another, including approval state.
 */
data class SharedActivity(
    val id: Long,
    val activityId: Long,
    val senderId: Long,
    val receiverId: Long,
    val status: String,
    val sharedPlan: Boolean
)

/**
 * Scored compatibility between the current user and another traveler for a given context.
 */
data class CompatibilityMatch(
    val userId: Long,
    val totalScore: Double,
    val destinationScore: Double,
    val dateProximityScore: Double,
    val interestScore: Double,
    val matchedInterests: List<String>
)
