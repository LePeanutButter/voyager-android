package com.voyager.tourism.domain.usecase.auth

import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserRole
import com.voyager.tourism.data.dto.UserStatus
import javax.inject.Inject

/**
 * Use case for getting user profile information
 * Encapsulates the business logic for retrieving user data
 */
class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return Result containing UserDto or error
     */
    suspend operator fun invoke(userId: String): Result<UserDto> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be empty"))
        }
        
        return try {
            val userResult = userRepository.getUserById(userId)
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                if (user != null) {
                    val userDto = UserDto(
                        id = user.id.toLongOrNull() ?: 0L,
                        username = user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        phoneNumber = user.phoneNumber,
                        role = UserRole.fromValue(user.role),
                        status = UserStatus.fromValue(user.status),
                        profileImageUrl = user.profileImageUrl,
                        bio = user.bio,
                        interests = user.interests,
                        dateOfBirth = user.dateOfBirth,
                        createdAt = user.createdAt,
                        updatedAt = user.updatedAt,
                        token = user.token
                    )
                    Result.success(userDto)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.failure(userResult.exceptionOrNull() ?: Exception("Failed to get user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
