package com.voyager.tourism.domain.usecase.auth

import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.repository.AuthRepository
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.local.PreferencesManager
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
    private val preferencesManager: PreferencesManager,
    private val moshi: Moshi,
    private val userMapper: UserMapper,
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
                
                userDto.token?.let { tokenManager.saveToken(it) }
                preferencesManager.saveCurrentUserId(userDto.id.toString())
                val userJson = moshi.adapter(com.voyager.tourism.data.dto.UserDto::class.java).toJson(userDto)
                tokenManager.saveUser(userJson)
                
                Result.success(userMapper.toDomain(userDto))
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
            val result = authRepository.exchangeGoogleCode(code, state)
            if (result.isSuccess) {
                val userDto = result.getOrThrow()
                userDto.token?.let { tokenManager.saveToken(it) }
                preferencesManager.saveCurrentUserId(userDto.id.toString())
                val userJson = moshi.adapter(UserDto::class.java).toJson(userDto)
                tokenManager.saveUser(userJson)
                Result.success(userMapper.toDomain(userDto))
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Google OAuth2 failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
