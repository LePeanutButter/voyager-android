package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.dto.UserDto
/**
 * Repository interface for authentication operations
 * Defines the contract for authentication data layer
 */
interface AuthRepository {
    
    /**
     * Register a new user account
     * @param username User's unique username
     * @param email User's email address
     * @param password User's password
     * @param firstName User's first name
     * @param lastName User's last name
     * @return Result containing the created UserDto or error
     */
    suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<UserDto>
    
    /**
     * Authenticate user with credentials
     * @param usernameOrEmail User's username or email
     * @param password User's password
     * @return Result containing the authenticated UserDto or error
     */
    suspend fun loginUser(
        usernameOrEmail: String,
        password: String
    ): Result<UserDto>
    
    /**
     * Handle Google OAuth2 callback
     */
    suspend fun handleGoogleCallback(code: String, state: String): Result<UserDto>

    /**
     * Starts the Google OAuth2 flow and returns an authorization URL or intermediate token from the backend.
     */
    suspend fun initiateGoogleLogin(): Result<String>
}
