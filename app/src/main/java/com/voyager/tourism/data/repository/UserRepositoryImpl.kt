package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.database.dao.TripDao
import com.voyager.tourism.data.database.dao.UserDao
import com.voyager.tourism.data.dto.AiUserPreferencesBody
import com.voyager.tourism.data.dto.LoginResponseDto
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserLoginDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.data.dto.UserUpdateDto
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.domain.model.BudgetRange
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.repository.UserRepository
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User profiles via **voyager-backend-core**; travel preferences are mirrored to **voyager-ai-service** (`/users/preferences/{id}`).
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val voyagerAiRepository: VoyagerAiRepository,
    private val userDao: UserDao,
    private val tripDao: TripDao,
    private val userMapper: UserMapper,
    private val preferencesManager: PreferencesManager,
) : UserRepository {

    private companion object {
        const val INVALID_USER_ID_MSG = "userId inválido"
    }

    /** @see UserRepository.getCurrentUser */
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val token = preferencesManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                Result.success(null)
            } else {
                val userId = preferencesManager.getCurrentUserId()?.toLongOrNull()
                if (userId == null) {
                    val localUser = userDao.getCurrentUser()
                    localUser?.let { Result.success(userMapper.entityToDomain(it)) }
                        ?: Result.success(null)
                } else {
                    val response = userApiService.getUserById(userId)
                    if (response.status == 200 && response.data != null) {
                        val userDto = response.data
                        userDao.insertUser(userMapper.toEntity(userDto))
                        Result.success(userMapper.toDomain(userDto))
                    } else {
                        val localUser = userDao.getCurrentUser()
                        localUser?.let { Result.success(userMapper.entityToDomain(it)) }
                            ?: Result.failure(Exception(response.message))
                    }
                }
            }
        } catch (e: Exception) {
            val localUser = userDao.getCurrentUser()
            localUser?.let { Result.success(userMapper.entityToDomain(it)) }
                ?: Result.failure(e)
        }
    }

    /** @see UserRepository.getUserById */
    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val id = userId.toLongOrNull()
                ?: return Result.failure(IllegalArgumentException(INVALID_USER_ID_MSG))
            val response = userApiService.getUserById(id)
            if (response.status == 200 && response.data != null) {
                val userDto = response.data
                val user = userMapper.toDomain(userDto)
                userDao.insertUser(userMapper.toEntity(userDto))
                Result.success(user)
            } else {
                val localUser = userDao.getUserById(userId)
                localUser?.let { Result.success(userMapper.entityToDomain(it)) }
                    ?: Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            val localUser = userDao.getUserById(userId)
            localUser?.let { Result.success(userMapper.entityToDomain(it)) }
                ?: Result.failure(e)
        }
    }

    /** @see UserRepository.authenticate */
    override suspend fun authenticate(email: String, password: String): Result<User> {
        return try {
            val response = userApiService.loginUser(
                UserLoginDto(usernameOrEmail = email, password = password),
            )
            if (response.status == 200 && response.data != null) {
                val login = response.data
                val userDto = login.user ?: return Result.failure(Exception("Login missing user data"))
                val dto = userDto.copy(token = login.token)
                dto.token?.let { preferencesManager.saveAuthToken(it) }
                preferencesManager.saveCurrentUserId(dto.id.toString())
                userDao.insertUser(userMapper.toEntity(dto))
                Result.success(userMapper.toDomain(dto))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see UserRepository.register */
    override suspend fun register(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String,
    ): Result<User> {
        return try {
            val response = userApiService.registerUser(
                UserRegistrationDto(
                    username = username,
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = null,
                ),
            )
            if ((response.status == 200 || response.status == 201) && response.data != null) {
                val dto = response.data
                userDao.insertUser(userMapper.toEntity(dto))
                preferencesManager.saveCurrentUserId(dto.id.toString())
                Result.success(userMapper.toDomain(dto))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see UserRepository.updateUser */
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val id = user.id.toLongOrNull()
                ?: return Result.failure(IllegalArgumentException("id inválido"))
            val response = userApiService.updateUser(id, userMapper.toUserUpdateDto(user))
            if (response.status == 200 && response.data != null) {
                val updated = response.data
                userDao.updateUser(userMapper.toEntity(updated))
                Result.success(userMapper.toDomain(updated))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see UserRepository.updateUserPreferences */
    override suspend fun updateUserPreferences(
        userId: String,
        preferences: com.voyager.tourism.domain.model.UserPreferences,
    ): Result<User> {
        return try {
            val body = AiUserPreferencesBody(
                preferences = preferences.interests.map { it.name },
                budgetRange = budgetRangeMap(preferences.budgetRange),
                travelStyle = preferences.travelStyle.name.lowercase(),
                groupSize = 2,
                accessibilityNeeds = emptyList(),
                dietaryRestrictions = emptyList(),
                languagePreferences = listOf("English"),
            )
            val aiResp = voyagerAiRepository.postUserPreferences(userId, body)
            if (!aiResp.isSuccessful) {
                return Result.failure(Exception("Preferencias IA: HTTP ${aiResp.code()}"))
            }
            val uid = userId.toLongOrNull()
                ?: return Result.failure(IllegalArgumentException(INVALID_USER_ID_MSG))
            val response = userApiService.getUserById(uid)
            if (response.status == 200 && response.data != null) {
                val dto = response.data
                userDao.updateUser(userMapper.toEntity(dto))
                Result.success(userMapper.toDomain(dto))
            } else {
                Result.failure(Exception(response.message.ifBlank { "No se pudo refrescar el usuario" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Maps coarse [BudgetRange] choices to numeric min/max pairs for the AI preference payload.
     */
    private fun budgetRangeMap(range: BudgetRange): Map<String, Double> = when (range) {
        BudgetRange.LOW -> mapOf("min" to 0.0, "max" to 80.0)
        BudgetRange.MEDIUM -> mapOf("min" to 50.0, "max" to 200.0)
        BudgetRange.HIGH -> mapOf("min" to 150.0, "max" to 500.0)
        BudgetRange.LUXURY -> mapOf("min" to 400.0, "max" to 5000.0)
    }

    /** @see UserRepository.logout */
    override suspend fun logout(): Result<Unit> {
        return try {
            val uid = preferencesManager.getCurrentUserId()
            preferencesManager.clearAuthData()
            if (!uid.isNullOrBlank()) {
                tripDao.deleteAllUserTrips(uid)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            preferencesManager.clearAuthData()
            Result.success(Unit)
        }
    }

    /** @see UserRepository.isUserAuthenticated */
    override fun isUserAuthenticated(): Flow<Boolean> {
        return preferencesManager.authTokenFlow.map { token ->
            token != null
        }
    }

    /** @see UserRepository.deleteUser */
    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val id = userId.toLongOrNull()
                ?: return Result.failure(IllegalArgumentException(INVALID_USER_ID_MSG))
            val response = userApiService.deleteUser(id)
            if (response.status == 200) {
                userDao.deleteUserById(userId)
                tripDao.deleteAllUserTrips(userId)
                preferencesManager.clearAuthData()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Applies a partial profile update via [UserUpdateDto] and returns the server payload.
     */
    override suspend fun updateUser(
        userId: String,
        firstName: String,
        lastName: String,
        bio: String,
        phoneNumber: String?,
        interests: List<String>,
    ): Result<UserDto> {
        return try {
            if (preferencesManager.getAuthToken().isNullOrEmpty()) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val id = userId.toLongOrNull()
                    ?: return Result.failure(IllegalArgumentException("userId inválido"))
                val request = UserUpdateDto(
                    firstName = firstName,
                    lastName = lastName,
                    bio = bio,
                    phoneNumber = phoneNumber,
                    interests = interests,
                )
                val response = userApiService.updateUser(id, request)
                if (response.status == 200 && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
