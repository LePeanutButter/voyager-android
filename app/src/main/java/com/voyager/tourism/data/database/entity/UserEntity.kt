package com.voyager.tourism.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity for User table
 * Represents local storage of user data
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatar: String? = null,
    val preferences: String? = null, // JSON string of UserPreferences
    val isVerified: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
