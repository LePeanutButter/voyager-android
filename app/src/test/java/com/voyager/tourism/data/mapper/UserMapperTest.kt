package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.database.entity.UserEntity
import com.voyager.tourism.data.dto.UserDto
import com.voyager.tourism.data.dto.UserRole
import com.voyager.tourism.data.dto.UserStatus
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.util.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class UserMapperTest {

    private val mapper = UserMapper()

    @Test
    fun `toDomain maps every DTO field`() {
        val dto = TestFixtures.userDto(id = 100L)
        val domain = mapper.toDomain(dto)

        assertEquals("100", domain.id)
        assertEquals(dto.email, domain.email)
        assertEquals(dto.username, domain.username)
        assertEquals(dto.firstName, domain.firstName)
        assertEquals(dto.lastName, domain.lastName)
        assertEquals(dto.role.value, domain.role)
        assertEquals(dto.status.value, domain.status)
        assertEquals(dto.token, domain.token)
    }

    @Test
    fun `toDto maps domain back to transport format`() {
        val domain = TestFixtures.domainUser().copy(id = "200")
        val dto = mapper.toDto(domain)

        assertEquals(200L, dto.id)
        assertEquals(domain.email, dto.email)
        assertEquals(UserRole.TRAVELER, dto.role)
        assertEquals(UserStatus.ACTIVE, dto.status)
    }

    @Test
    fun `toEntity from DTO handles verified status and timestamps`() {
        val dto = TestFixtures.userDto(id = 5L).copy(
            status = UserStatus.ACTIVE,
            createdAt = "2024-05-01T10:00:00Z"
        )
        val entity = mapper.toEntity(dto)

        assertEquals("5", entity.id)
        assertTrue(entity.isVerified)
        assertTrue(entity.createdAt > 0L)
    }

    @Test
    fun `toEntity from Domain handles status casing`() {
        val domain = TestFixtures.domainUser().copy(status = "active")
        val entity = mapper.toEntity(domain)
        assertTrue(entity.isVerified)
    }

    @Test
    fun `entityToDomain synthesizes ISO strings from epoch`() {
        val now = System.currentTimeMillis()
        val entity = UserEntity(
            id = "1",
            username = "u",
            email = "e",
            firstName = "F",
            lastName = "L",
            avatar = "a",
            preferences = null,
            isVerified = true,
            createdAt = now,
            updatedAt = now
        )
        val domain = mapper.entityToDomain(entity)

        assertEquals("1", domain.id)
        assertNotNull(domain.createdAt)
        assertTrue(domain.createdAt!!.contains("Z"))
    }

    @Test
    fun `toUserUpdateDto picks editable fields`() {
        val domain = TestFixtures.domainUser().copy(bio = "My bio")
        val update = mapper.toUserUpdateDto(domain)

        assertEquals(domain.firstName, update.firstName)
        assertEquals("My bio", update.bio)
    }

    @Test
    fun `parseBackendInstantToMillis handles malformed or null strings`() {
        val dtoNull = TestFixtures.userDto().copy(createdAt = null)
        val entityNull = mapper.toEntity(dtoNull)
        assertTrue(entityNull.createdAt > 0)

        val dtoBad = TestFixtures.userDto().copy(createdAt = "not-a-date")
        val entityBad = mapper.toEntity(dtoBad)
        assertTrue(entityBad.createdAt > 0)

        val dtoLocal = TestFixtures.userDto().copy(createdAt = "2024-01-01T10:00:00")
        val entityLocal = mapper.toEntity(dtoLocal)
        assertTrue(entityLocal.createdAt > 0)
    }
}
