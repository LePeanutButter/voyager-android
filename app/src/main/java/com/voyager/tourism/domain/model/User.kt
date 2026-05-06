package com.voyager.tourism.domain.model

/**
 * Domain model representing a user in the Tourism Intelligent Platform
 * Matches backend UserDto structure for consistency
 */
data class User(
    val id: String, // Long from backend converted to String
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String? = null,
    val role: String, // UserRole enum from backend converted to String
    val status: String, // UserStatus enum from backend converted to String
    val profileImageUrl: String? = null,
    val bio: String? = null,
    val interests: Set<String> = emptySet(),
    val dateOfBirth: String? = null, // LocalDateTime from backend
    val createdAt: String? = null, // LocalDateTime from backend
    val updatedAt: String? = null, // LocalDateTime from backend
    val token: String? = null // JWT token
)

/**
 * User preferences for personalized recommendations
 */
data class UserPreferences(
    val preferredDestinations: List<String> = emptyList(),
    val travelStyle: TravelStyle = TravelStyle.BALANCED,
    val budgetRange: BudgetRange = BudgetRange.MEDIUM,
    val interests: List<TravelInterest> = emptyList(),
    val accommodationType: AccommodationType = AccommodationType.HOTEL,
    val notificationsEnabled: Boolean = true
)

enum class TravelStyle {
    ADVENTURE, LUXURY, BUDGET, CULTURAL, RELAXED, BALANCED
}

enum class BudgetRange {
    LOW, MEDIUM, HIGH, LUXURY
}

enum class TravelInterest {
    HISTORY, NATURE, FOOD, NIGHTLIFE, SHOPPING, ART, SPORTS, SPIRITUAL, FAMILY
}

enum class AccommodationType {
    HOTEL, HOSTEL, AIRBNB, RESORT, CAMPING, APARTMENT
}
