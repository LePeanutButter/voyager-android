package com.voyager.tourism.data.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.voyager.tourism.data.database.entity.UserEntity

/**
 * Data Access Object for User operations
 * Provides database access methods for user entities
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM users LIMIT 1)")
    fun hasUser(): Flow<Boolean>
}
