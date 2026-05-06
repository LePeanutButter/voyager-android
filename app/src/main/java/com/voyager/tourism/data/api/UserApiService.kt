package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.PagedResponseUserDto
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserLoginDto
import com.voyager.tourism.data.dto.UserRegistrationDto
import com.voyager.tourism.data.dto.UserStatisticsDto
import com.voyager.tourism.data.dto.UserUpdateDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de [com.tourism.platform.controller.UserController].
 * Base Retrofit: [com.voyager.tourism.BuildConfig.BACKEND_BASE_URL] (`…/api/v1/`).
 */
interface UserApiService {

    @POST("users")
    suspend fun registerUser(@Body body: UserRegistrationDto): ApiResponse<UserDto>

    @POST("users/register")
    suspend fun registerUserAlias(@Body body: UserRegistrationDto): ApiResponse<UserDto>

    @POST("users/login")
    suspend fun loginUser(@Body body: UserLoginDto): ApiResponse<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): ApiResponse<UserDto>

    @GET("users/username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): ApiResponse<UserDto>

    @GET("users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): ApiResponse<UserDto>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body body: UserUpdateDto): ApiResponse<UserDto>

    @PUT("users/{id}/password")
    suspend fun changePassword(
        @Path("id") id: Long,
        @Query("currentPassword") currentPassword: String,
        @Query("newPassword") newPassword: String,
    ): ApiResponse<Unit>

    @GET("users")
    suspend fun getAllUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc",
    ): PagedResponseUserDto

    @GET("users/role/{role}")
    suspend fun getUsersByRole(
        @Path("role") role: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseUserDto

    @GET("users/status/{status}")
    suspend fun getUsersByStatus(
        @Path("status") status: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseUserDto

    @GET("users/search")
    suspend fun searchUsersByName(
        @Query("searchTerm") searchTerm: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseUserDto

    @PUT("users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: Long,
        @Query("role") role: String,
    ): ApiResponse<UserDto>

    @PUT("users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") id: Long,
        @Query("status") status: String,
    ): ApiResponse<UserDto>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): ApiResponse<Unit>

    @GET("users/statistics")
    suspend fun getUserStatistics(): ApiResponse<UserStatisticsDto>

    @GET("users/check-username")
    suspend fun checkUsernameAvailability(@Query("username") username: String): ApiResponse<Boolean>

    @GET("users/check-email")
    suspend fun checkEmailAvailability(@Query("email") email: String): ApiResponse<Boolean>
}
