package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.database.entity.UserEntity
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserRole
import com.voyager.tourism.data.dto.UserStatus
import com.voyager.tourism.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting between different User representations
 * Handles conversion between domain model, DTO, and entity
 * Correctly handles Long IDs and enums from backend
 */
@Singleton
class UserMapper @Inject constructor() {
    
    fun toDomain(userDto: UserDto): User {
        return User(
            id = userDto.id.toString(), // Convert Long to String for domain
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
            token = userDto.token
        )
    }
    
    fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id.toLongOrNull() ?: 0L, // Convert String to Long for backend
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
            token = user.token
        )
    }
    
    fun toEntity(userDto: UserDto): UserEntity {
        return UserEntity(
            id = userDto.id.toString(),
            username = userDto.username,
            email = userDto.email,
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
            updatedAt = userDto.updatedAt
        )
    }
    
    fun toEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber,
            role = user.role,
            status = user.status,
            profileImageUrl = user.profileImageUrl,
            bio = user.bio,
            interests = user.interests,
            dateOfBirth = user.dateOfBirth,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
    
    fun entityToDomain(userEntity: UserEntity): User {
        return User(
            id = userEntity.id,
            email = userEntity.email,
            username = userEntity.username,
            firstName = userEntity.firstName,
            lastName = userEntity.lastName,
            phoneNumber = userEntity.phoneNumber,
            role = userEntity.role,
            status = userEntity.status,
            profileImageUrl = userEntity.profileImageUrl,
            bio = userEntity.bio,
            interests = userEntity.interests,
            dateOfBirth = userEntity.dateOfBirth,
            createdAt = userEntity.createdAt,
            updatedAt = userEntity.updatedAt,
            token = null // Entity doesn't store token
        )
    }
}
