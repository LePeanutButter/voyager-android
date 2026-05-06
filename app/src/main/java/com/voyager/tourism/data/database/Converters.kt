package com.voyager.tourism.data.database

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.voyager.tourism.data.dto.CoordinatesDto
import com.voyager.tourism.data.dto.DestinationDto
import com.voyager.tourism.data.dto.UserPreferencesDto

/**
 * Type converters for Room database
 * Handles conversion of complex types to/from database primitives
 */
class Converters {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Serializes a list of strings to JSON for Room storage.
     *
     * @param value Non-null list (may be empty).
     * @return JSON array string.
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = moshi.adapter(listType)
        return adapter.toJson(value)
    }

    /**
     * Deserializes a JSON array of strings from the database.
     *
     * @param value JSON string from [fromStringList].
     * @return Parsed list, or empty list if parsing yields `null`.
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter: JsonAdapter<List<String>> = moshi.adapter(listType)
        return adapter.fromJson(value) ?: emptyList()
    }

    /**
     * Serializes [DestinationDto] to JSON for Room.
     *
     * @param destination Domain transfer object to persist.
     * @return JSON string.
     */
    @TypeConverter
    fun fromDestinationDto(destination: DestinationDto): String {
        val adapter: JsonAdapter<DestinationDto> = moshi.adapter(DestinationDto::class.java)
        return adapter.toJson(destination)
    }

    /**
     * Deserializes [DestinationDto] from JSON stored in Room.
     *
     * @param value JSON string; if parsing fails, returns a placeholder empty destination.
     */
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

    /**
     * Serializes [UserPreferencesDto] to JSON for Room.
     *
     * @param preferences User preferences snapshot.
     * @return JSON string.
     */
    @TypeConverter
    fun fromUserPreferencesDto(preferences: UserPreferencesDto): String {
        val adapter: JsonAdapter<UserPreferencesDto> = moshi.adapter(UserPreferencesDto::class.java)
        return adapter.toJson(preferences)
    }

    /**
     * Deserializes [UserPreferencesDto] from JSON; falls back to defaults if parsing returns `null`.
     *
     * @param value JSON string from [fromUserPreferencesDto].
     */
    @TypeConverter
    fun toUserPreferencesDto(value: String): UserPreferencesDto {
        val adapter: JsonAdapter<UserPreferencesDto> = moshi.adapter(UserPreferencesDto::class.java)
        return adapter.fromJson(value) ?: UserPreferencesDto()
    }
}
