package com.voyager.tourism.domain.repository

import com.voyager.tourism.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user-related operations
 * Defines the contract for data layer implementation
 */
interface UserRepository {
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): Result<User?>
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<User?>
    
    /**
     * Authenticate user with email and password
     */
    suspend fun authenticate(email: String, password: String): Result<User>
    
    /**
     * Register new user
     */
    suspend fun register(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String
    ): Result<User>
    
    /**
     * Update user profile
     */
    suspend fun updateUser(user: User): Result<User>
    
    /**
     * Update user preferences
     */
    suspend fun updateUserPreferences(
        userId: String,
        preferences: com.voyager.tourism.domain.model.UserPreferences
    ): Result<User>
    
    /**
     * Logout user
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Check if user is authenticated
     */
    fun isUserAuthenticated(): Flow<Boolean>
    
    /**
     * Delete user account
     */
    suspend fun deleteUser(userId: String): Result<Unit>
    
        
    /**
     * Update user profile
     */
    suspend fun updateUser(
        userId: String,
        firstName: String,
        lastName: String,
        bio: String,
        phoneNumber: String?,
        interests: List<String>
    ): Result<com.voyager.tourism.data.dto.UserDto>
}
