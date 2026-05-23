package com.voyager.tourism.data.api

import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.dto.LoginResponseDto
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
 * Retrofit contract for [com.tourism.platform.controller.UserController] endpoints.
 * Base URL: [com.voyager.tourism.BuildConfig.BACKEND_BASE_URL] (`…/api/v1/`).
 */
interface UserApiService {

    /**
     * Registers a new user with standard credentials.
     */
    @POST("users")
    suspend fun registerUser(@Body body: UserRegistrationDto): ApiResponse<UserDto>

    /**
     * Alternate registration route exposed for backwards compatibility.
     */
    @POST("users/register")
    suspend fun registerUserAlias(@Body body: UserRegistrationDto): ApiResponse<UserDto>

    /**
     * Authenticates a user with email/username and password.
     */
    @POST("users/login")
    suspend fun loginUser(@Body body: UserLoginDto): ApiResponse<UserDto>

    /**
     * Fetches a user profile by numeric identifier.
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): ApiResponse<UserDto>

    /**
     * Resolves a profile using the unique username.
     */
    @GET("users/username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): ApiResponse<UserDto>

    /**
     * Resolves a profile using the email address.
     */
    @GET("users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): ApiResponse<UserDto>

    /**
     * Updates mutable user fields for the given account id.
     */
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body body: UserUpdateDto): ApiResponse<UserDto>

    /**
     * Changes password after verifying the current password value.
     */
    @PUT("users/{id}/password")
    suspend fun changePassword(
        @Path("id") id: Long,
        @Query("currentPassword") currentPassword: String,
        @Query("newPassword") newPassword: String,
    ): ApiResponse<Unit>

    /**
     * Returns an admin-style page of users with optional sorting.
     */
    @GET("users")
    suspend fun getAllUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc",
    ): PagedResponseUserDto

    /**
     * Filters users by the textual role key.
     */
    @GET("users/role/{role}")
    suspend fun getUsersByRole(
        @Path("role") role: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseUserDto

    /**
     * Filters users by lifecycle status.
     */
    @GET("users/status/{status}")
    suspend fun getUsersByStatus(
        @Path("status") status: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseUserDto

    /**
     * Performs a name search against first/last name fields.
     */
    @GET("users/search")
    suspend fun searchUsersByName(
        @Query("searchTerm") searchTerm: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponseUserDto

    /**
     * Promotes or demotes a user's authorization role.
     */
    @PUT("users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: Long,
        @Query("role") role: String,
    ): ApiResponse<UserDto>

    /**
     * Updates account status flags such as `ACTIVE` or `SUSPENDED`.
     */
    @PUT("users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") id: Long,
        @Query("status") status: String,
    ): ApiResponse<UserDto>

    /**
     * Deletes a user row permanently.
     */
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): ApiResponse<Unit>

    /**
     * Returns aggregate counts for dashboards and analytics tiles.
     */
    @GET("users/statistics")
    suspend fun getUserStatistics(): ApiResponse<UserStatisticsDto>

    /**
     * Checks whether a username is still available for registration.
     */
    @GET("users/check-username")
    suspend fun checkUsernameAvailability(@Query("username") username: String): ApiResponse<Boolean>

    /**
     * Checks whether an email is still available for registration.
     */
    @GET("users/check-email")
    suspend fun checkEmailAvailability(@Query("email") email: String): ApiResponse<Boolean>
}
