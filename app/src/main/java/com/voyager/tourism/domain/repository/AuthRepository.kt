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
     * Exchanges a Google authorization code (from native SDK or redirect) for a session token.
     * @param code The server authorization code
     * @param state Optional state for CSRF protection (mainly for web/redirect flow)
     * @return Result containing the authenticated UserDto or error
     */
    suspend fun exchangeGoogleCode(code: String, state: String? = null): Result<UserDto>

    /**
     * Starts the Google OAuth2 flow and returns an authorization URL or intermediate token from the backend.
     */
    suspend fun initiateGoogleLogin(): Result<String>
}
