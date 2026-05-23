package com.voyager.tourism.domain.usecase.auth

import com.squareup.moshi.Moshi
import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.local.TokenManager
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user registration
 * Encapsulates the business logic for creating a new user account
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val preferencesManager: PreferencesManager,
    private val moshi: Moshi,
    private val userMapper: UserMapper,
) {
    
    /**
     * Execute user registration
     * @param username User's unique username
     * @param email User's email address
     * @param password User's password
     * @param firstName User's first name
     * @param lastName User's last name
     * @return Result containing the created User or error
     */
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<User> {
        return try {
            val userDtoResult = authRepository.registerUser(username, email, password, firstName, lastName)
            
            if (userDtoResult.isSuccess) {
                val userDto = userDtoResult.getOrThrow()
                userDto.token?.let { tokenManager.saveToken(it) }
                preferencesManager.saveCurrentUserId(userDto.id.toString())
                tokenManager.saveUser(moshi.adapter(UserDto::class.java).toJson(userDto))
                Result.success(userMapper.toDomain(userDto))
            } else {
                Result.failure(userDtoResult.exceptionOrNull() ?: Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
