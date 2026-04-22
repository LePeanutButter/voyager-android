package com.voyager.tourism.domain.model

/**
 * Domain model representing a user in the Tourism Intelligent Platform
 */
data class User(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatar: String? = null,
    val preferences: UserPreferences? = null,
    val isVerified: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
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
