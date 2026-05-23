package com.voyager.tourism.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.voyager.tourism.data.database.entity.UserEntity

/**
 * Room DAO for locally cached user profile rows.
 */
@Dao
interface UserDao {
    
    /**
     * Loads a user by primary key, or null if absent.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    /**
     * Loads a user by email address, or null if absent.
     */
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    /**
     * Returns the first user row if any exists, typically the signed-in profile.
     */
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    /**
     * Inserts or replaces a user row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    /**
     * Updates fields on an existing user row.
     */
    @Update
    suspend fun updateUser(user: UserEntity)
    
    /**
     * Deletes the given user row.
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    /**
     * Deletes a user by primary key.
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    /**
     * Observes all cached users as a cold [Flow].
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    /**
     * Emits whether at least one user row exists in the table.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users LIMIT 1)")
    fun hasUser(): Flow<Boolean>
}
