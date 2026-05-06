package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.database.entity.UserEntity
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserRole
import com.voyager.tourism.data.dto.UserStatus
import com.voyager.tourism.data.dto.UserUpdateDto
import com.voyager.tourism.domain.model.User
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting between different User representations
 * Handles conversion between domain model, DTO, and entity
 * Correctly handles Long IDs and enums from backend
 */
@Singleton
class UserMapper @Inject constructor() {

    /**
     * Converts a wire-format [UserDto] into the rich domain [User] model.
     */
    fun toDomain(userDto: UserDto): User {
        return User(
            id = userDto.id.toString(),
            email = userDto.email,
            username = userDto.username,
            firstName = userDto.firstName,
            lastName = userDto.lastName,
            phoneNumber = userDto.phoneNumber,
            role = userDto.role.value,
            status = userDto.status.value,
            profileImageUrl = userDto.profileImageUrl,
            bio = userDto.bio,
            interests = userDto.interests?.toSet() ?: emptySet(),
            dateOfBirth = userDto.dateOfBirth,
            createdAt = userDto.createdAt,
            updatedAt = userDto.updatedAt,
            token = userDto.token,
        )
    }

    /**
     * Serializes a domain [User] back into transport DTO form for API calls.
     */
    fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id.toLongOrNull() ?: 0L,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber,
            role = UserRole.fromValue(user.role),
            status = UserStatus.fromValue(user.status),
            profileImageUrl = user.profileImageUrl,
            bio = user.bio,
            interests = user.interests,
            dateOfBirth = user.dateOfBirth,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            token = user.token,
        )
    }

    /**
     * Maps a network [UserDto] into a Room [UserEntity] row for offline caching.
     */
    fun toEntity(userDto: UserDto): UserEntity {
        return UserEntity(
            id = userDto.id.toString(),
            username = userDto.username,
            email = userDto.email,
            firstName = userDto.firstName,
            lastName = userDto.lastName,
            avatar = userDto.profileImageUrl,
            preferences = null,
            isVerified = userDto.status == UserStatus.ACTIVE,
            createdAt = parseBackendInstantToMillis(userDto.createdAt),
            updatedAt = parseBackendInstantToMillis(userDto.updatedAt),
        )
    }

    /**
     * Maps a domain [User] into a Room [UserEntity] snapshot.
     */
    fun toEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            avatar = user.profileImageUrl,
            preferences = null,
            isVerified = user.status.equals(UserStatus.ACTIVE.value, ignoreCase = true),
            createdAt = parseBackendInstantToMillis(user.createdAt),
            updatedAt = parseBackendInstantToMillis(user.updatedAt),
        )
    }

    /**
     * Builds an update payload for PATCH/PUT endpoints from editable profile fields.
     */
    fun toUserUpdateDto(user: User): UserUpdateDto {
        return UserUpdateDto(
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber,
            profileImageUrl = user.profileImageUrl,
            bio = user.bio,
            interests = user.interests.toList(),
            dateOfBirth = user.dateOfBirth,
        )
    }

    /**
     * Converts a cached [UserEntity] into a domain [User], synthesizing ISO timestamps from millis.
     */
    fun entityToDomain(userEntity: UserEntity): User {
        val created = Instant.ofEpochMilli(userEntity.createdAt).toString()
        val updated = Instant.ofEpochMilli(userEntity.updatedAt).toString()
        return User(
            id = userEntity.id,
            email = userEntity.email,
            username = userEntity.username,
            firstName = userEntity.firstName,
            lastName = userEntity.lastName,
            phoneNumber = null,
            role = UserRole.USER.value,
            status = if (userEntity.isVerified) UserStatus.ACTIVE.value else UserStatus.PENDING.value,
            profileImageUrl = userEntity.avatar,
            bio = null,
            interests = emptySet(),
            dateOfBirth = null,
            createdAt = created,
            updatedAt = updated,
            token = null,
        )
    }

    /**
     * Parses backend ISO-8601 timestamps into epoch millis, falling back to "now" when malformed.
     */
    private fun parseBackendInstantToMillis(iso: String?): Long {
        if (iso.isNullOrBlank()) return System.currentTimeMillis()
        runCatching { Instant.parse(iso) }.getOrNull()?.let { return it.toEpochMilli() }
        runCatching {
            val local = LocalDateTime.parse(iso.take(19))
            local.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrNull()?.let { return it }
        return System.currentTimeMillis()
    }
}
