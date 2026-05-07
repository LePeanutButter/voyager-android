package com.voyager.tourism.domain.usecase.auth

import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.data.dto.UserDto
import javax.inject.Inject

/**
 * Use case for getting user profile information
 * Encapsulates the business logic for retrieving user data
 */
class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
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
                    Result.success(userMapper.toDto(user))
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
