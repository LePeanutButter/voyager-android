package com.voyager.tourism.domain.usecase.auth

import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use case for user authentication
 * Encapsulates the business logic for login functionality
 */
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    
    /**
     * Execute login with email and password
     * @param email User email address
     * @param password User password
     * @return Result containing authenticated User or error
     */
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validate input
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        
        return try {
            userRepository.authenticate(email, password)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Basic email validation
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}
