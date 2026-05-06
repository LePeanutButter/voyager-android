package com.voyager.tourism.data.database

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.voyager.tourism.data.dto.CoordinatesDto
import com.voyager.tourism.data.dto.DestinationDto
import com.voyager.tourism.data.dto.UserPreferencesDto

/**
 * Type converters for Room database
 * Handles conversion of complex types to/from database primitives
 */
class Converters {
    
    private val moshi = Moshi.Builder().build()
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = moshi.adapter(listType)
        return adapter.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = moshi.adapter(listType)
        return adapter.fromJson(value) ?: emptyList()
    }
    
    @TypeConverter
    fun fromDestinationDto(destination: DestinationDto): String {
        val adapter: JsonAdapter<DestinationDto> = moshi.adapter(DestinationDto::class.java)
        return adapter.toJson(destination)
    }
    
    @TypeConverter
    fun toDestinationDto(value: String): DestinationDto {
        val adapter: JsonAdapter<DestinationDto> = moshi.adapter(DestinationDto::class.java)
        return adapter.fromJson(value) ?: DestinationDto(
            id = "",
            name = "",
            country = "",
            coordinates = CoordinatesDto(0.0, 0.0),
            timezone = "",
            currency = "",
            language = "",
            climate = "",
            bestTimeToVisit = "",
            averageCost = 0.0,
            rating = 0f,
        )
    }
    
    @TypeConverter
    fun fromUserPreferencesDto(preferences: UserPreferencesDto): String {
        val adapter: JsonAdapter<UserPreferencesDto> = moshi.adapter(UserPreferencesDto::class.java)
        return adapter.toJson(preferences)
    }
    
    @TypeConverter
    fun toUserPreferencesDto(value: String): UserPreferencesDto {
        val adapter: JsonAdapter<UserPreferencesDto> = moshi.adapter(UserPreferencesDto::class.java)
        return adapter.fromJson(value) ?: UserPreferencesDto()
    }
}
