package com.voyager.tourism.domain.usecase.auth

import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.repository.AuthRepository
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.local.TokenManager
import com.squareup.moshi.Moshi
import javax.inject.Inject

/**
 * Use case for user authentication
 * Encapsulates the business logic for login functionality
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val moshi: Moshi
) {
    
    /**
     * Execute login with email or username and password
     * @param usernameOrEmail User username or email address
     * @param password User password
     * @return Result containing authenticated User or error
     */
    suspend operator fun invoke(usernameOrEmail: String, password: String): Result<User> {
        // Validate input
        if (usernameOrEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Username or email cannot be empty"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }
        
        return try {
            val userDtoResult = authRepository.loginUser(usernameOrEmail, password)
            
            if (userDtoResult.isSuccess) {
                val userDto = userDtoResult.getOrThrow()
                
                // Save token and user data
                userDto.token?.let { tokenManager.saveToken(it) }
                
                // Save user data as JSON
                val userJson = moshi.adapter(com.voyager.tourism.data.dto.UserDto::class.java).toJson(userDto)
                tokenManager.saveUser(userJson)
                
                // Convert to domain model
                val user = mapToDomainModel(userDto)
                Result.success(user)
            } else {
                Result.failure(userDtoResult.exceptionOrNull() ?: Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Execute Google OAuth2 login
     * @param code Authorization code from Google
     * @param state State parameter for CSRF protection
     * @return Result containing authenticated User or error
     */
    suspend fun loginWithGoogle(code: String, state: String? = null): Result<User> {
        return try {
            val response = authRepository.handleGoogleCallback(code, state ?: "")
            
            if (response.status == 200 && response.data != null) {
                val userDto = response.data!!
                
                // Save token and user data
                userDto.token?.let { tokenManager.saveToken(it) }
                
                // Save user data as JSON
                val userJson = moshi.adapter(UserDto::class.java).toJson(userDto)
                tokenManager.saveUser(userJson)
                
                // Convert to domain model
                val user = mapToDomainModel(userDto)
                Result.success(user)
            } else {
                Result.failure(Exception(response.message ?: "Google OAuth2 failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convert UserDto to domain User model
     */
    private fun mapToDomainModel(userDto: UserDto): User {
        return User(
            id = userDto.id.toString(),
            email = userDto.email,
            username = userDto.username,
            firstName = userDto.firstName,
            lastName = userDto.lastName,
            phoneNumber = userDto.phoneNumber,
            role = userDto.role.value,
            status = userDto.status.value,
            profileImageUrl = userDto.profileImageUrl,
            bio = userDto.bio,
            interests = userDto.interests ?: emptySet(),
            dateOfBirth = userDto.dateOfBirth,
            createdAt = userDto.createdAt,
            updatedAt = userDto.updatedAt,
            token = userDto.token
        )
    }
    
    /**
     * Basic email validation
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}
