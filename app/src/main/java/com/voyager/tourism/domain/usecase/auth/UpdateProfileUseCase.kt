package com.voyager.tourism.domain.usecase.auth

import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for updating user profile information
 * Encapsulates the business logic for profile updates
 */
class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    /**
     * Update user profile
     * @param userId User ID to update
     * @param firstName User's first name (required)
     * @param lastName User's last name (required)
     * @param bio User's biography (required, max 500 chars)
     * @param phoneNumber User's phone number (optional)
     * @param interests List of user's interests
     * @return Result containing updated UserDto or error
     */
    suspend operator fun invoke(
        userId: String,
        firstName: String,
        lastName: String,
        bio: String,
        phoneNumber: String?,
        interests: List<String>
    ): Result<UserDto> {
        // Validate required fields
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be empty"))
        }
        
        if (firstName.isBlank()) {
            return Result.failure(IllegalArgumentException("First name is required"))
        }
        
        if (lastName.isBlank()) {
            return Result.failure(IllegalArgumentException("Last name is required"))
        }
        
        if (bio.isBlank()) {
            return Result.failure(IllegalArgumentException("Biography is required"))
        }
        
        if (bio.length > 500) {
            return Result.failure(IllegalArgumentException("Biography must be 500 characters or less"))
        }
        
        return try {
            userRepository.updateUser(userId, firstName, lastName, bio, phoneNumber, interests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
