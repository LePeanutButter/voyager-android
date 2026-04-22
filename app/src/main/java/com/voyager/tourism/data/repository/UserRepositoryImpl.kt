package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.TourismApiService
import com.voyager.tourism.data.database.dao.UserDao
import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserRepository interface
 * Handles user data operations combining remote API and local database
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: TourismApiService,
    private val userDao: UserDao,
    private val userMapper: UserMapper,
    private val preferencesManager: PreferencesManager
) : UserRepository {
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                Result.success(null)
            } else {
                val response = apiService.getCurrentUser("Bearer $token")
                if (response.isSuccessful) {
                    val userDto = response.body()
                    userDto?.let {
                        val user = userMapper.toDomain(it)
                        // Cache user locally
                        userDao.insertUser(userMapper.toEntity(it))
                        Result.success(user)
                    } ?: Result.success(null)
                } else {
                    // Try to get from local cache if API fails
                    val localUser = userDao.getCurrentUser()
                    localUser?.let { Result.success(userMapper.toDomain(it)) }
                        ?: Result.failure(Exception("No user data available"))
                }
            }
        } catch (e: Exception) {
            // Try local cache as fallback
            val localUser = userDao.getCurrentUser()
            localUser?.let { Result.success(userMapper.toDomain(it)) }
                ?: Result.failure(e)
        }
    }
    
    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val token = preferencesManager.getAuthToken()
            val response = apiService.getCurrentUser("Bearer $token")
            if (response.isSuccessful) {
                val userDto = response.body()
                userDto?.let {
                    Result.success(userMapper.toDomain(it))
                } ?: Result.success(null)
            } else {
                // Try local database
                val localUser = userDao.getUserById(userId)
                localUser?.let { Result.success(userMapper.toDomain(it)) }
                    ?: Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            val localUser = userDao.getUserById(userId)
            localUser?.let { Result.success(userMapper.toDomain(it)) }
                ?: Result.failure(e)
        }
    }
    
    override suspend fun authenticate(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(
                com.voyager.tourism.data.dto.LoginRequest(email, password)
            )
            if (response.isSuccessful) {
                val loginResponse = response.body()
                loginResponse?.let {
                    // Save auth token
                    preferencesManager.saveAuthToken(it.token)
                    preferencesManager.saveRefreshToken(it.refreshToken)
                    
                    // Cache user locally
                    userDao.insertUser(userMapper.toEntity(it.user))
                    
                    Result.success(userMapper.toDomain(it.user))
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String
    ): Result<User> {
        return try {
            val response = apiService.register(
                com.voyager.tourism.data.dto.RegisterRequest(
                    email, password, username, firstName, lastName
                )
            )
            if (response.isSuccessful) {
                val userDto = response.body()
                userDto?.let {
                    // Cache user locally
                    userDao.insertUser(userMapper.toEntity(it))
                    Result.success(userMapper.toDomain(it))
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val token = preferencesManager.getAuthToken()
            val userDto = userMapper.toDto(user)
            val response = apiService.updateUser(user.id, userDto, "Bearer $token")
            if (response.isSuccessful) {
                val updatedUserDto = response.body()
                updatedUserDto?.let {
                    // Update local cache
                    userDao.updateUser(userMapper.toEntity(it))
                    Result.success(userMapper.toDomain(it))
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUserPreferences(
        userId: String,
        preferences: com.voyager.tourism.domain.model.UserPreferences
    ): Result<User> {
        return try {
            val token = preferencesManager.getAuthToken()
            val response = apiService.updateUserPreferences(userId, "Bearer $token")
            if (response.isSuccessful) {
                val userDto = response.body()
                userDto?.let {
                    userDao.updateUser(userMapper.toEntity(it))
                    Result.success(userMapper.toDomain(it))
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Preferences update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (!token.isNullOrEmpty()) {
                apiService.logout("Bearer $token")
            }
            // Clear local data
            preferencesManager.clearAuthData()
            userDao.deleteAllUserTrips(preferencesManager.getCurrentUserId())
            Result.success(Unit)
        } catch (e: Exception) {
            // Still clear local data even if API call fails
            preferencesManager.clearAuthData()
            Result.success(Unit)
        }
    }
    
    override fun isUserAuthenticated(): Flow<Boolean> {
        return preferencesManager.getAuthTokenFlow().map { token ->
            !token.isNullOrEmpty()
        }
    }
    
    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val token = preferencesManager.getAuthToken()
            // API call would go here
            // For now, just clear local data
            userDao.deleteUserById(userId)
            preferencesManager.clearAuthData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
