package com.voyager.tourism.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a destination in the local database
 */
@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val location: String,
    val imageUrl: String? = null,
    val rating: Float = 0.0f,
    val category: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
