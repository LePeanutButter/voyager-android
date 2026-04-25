package com.voyager.tourism.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    indices = [Index(value = ["travelPlanId"]), Index(value = ["startTime"])]
)
data class ActivityEntity(
    @PrimaryKey val id: String,
    val travelPlanId: String,
    val name: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val location: String?,
    val updatedAt: String?
)
