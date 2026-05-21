package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.api.GoogleAuthApiService
import com.voyager.tourism.data.dto.GoogleServerAuthRequest
import com.voyager.tourism.data.dto.LoginResponseDto
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserLoginDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository
 * Handles authentication data operations using the correct API services
 * Matches exactly the backend endpoints
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val googleAuthApiService: GoogleAuthApiService
) : AuthRepository {
    
    /**
     * Persists a new user by calling the registration endpoint and mapping success to [Result].
     */
    override suspend fun registerUser(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<UserDto> {
        return try {
            val request = UserRegistrationDto(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName
            )
            
            val response = userApiService.registerUser(request)
            
            if ((response.status == 200 || response.status == 201) && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Registration failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Authenticates with username/email and password via the REST login route.
     */
    override suspend fun loginUser(
        usernameOrEmail: String,
        password: String
    ): Result<UserDto> {
        return try {
            val request = UserLoginDto(
                usernameOrEmail = usernameOrEmail,
                password = password
            )
            
            val response = userApiService.loginUser(request)
            val loginData = response.data

            if (response.status == 200 && loginData != null) {
                // El backend actual ya incluye el token dentro del objeto UserDto en la raíz de 'data'
                // pero ahora usamos LoginResponseDto que separa el token del usuario.
                // Los mapeamos para cumplir el contrato de Result<UserDto>.
                val userDto = loginData.user.copy(token = loginData.token)
                Result.success(userDto)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Login failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Intercambia el código de Google (`serverAuthCode` nativo o `code` de deep link) vía POST [auth/google/token].
     * El parámetro [state] se conserva por compatibilidad con el flujo por URI; el backend actual no lo usa.
     */
    override suspend fun exchangeGoogleCode(code: String, state: String?): Result<UserDto> {
        return try {
            val response = googleAuthApiService.exchangeGoogleServerAuthCode(
                GoogleServerAuthRequest(code = code),
            )
            
            if (response.status == 200 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message.ifBlank { "Google OAuth2 failed" }))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Requests the backend URL used to begin the Google OAuth2 redirect flow.
     * Captured from the 'Location' header of the 302 redirect response.
     */
    override suspend fun initiateGoogleLogin(): Result<String> {
        return try {
            val response = googleAuthApiService.initiateGoogleLogin()
            val location = response.headers()["Location"]
            
            if ((response.code() == 302 || response.code() == 301 || response.code() == 200) && location != null) {
                Result.success(location)
            } else {
                Result.failure(Exception("No se pudo obtener la URL de redirección (Status: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
