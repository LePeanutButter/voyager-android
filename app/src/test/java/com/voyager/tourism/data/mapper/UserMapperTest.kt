package com.voyager.tourism.data.mapper

import com.voyager.tourism.data.database.entity.UserEntity
import com.voyager.tourism.data.dto.UserRole
import com.voyager.tourism.data.dto.UserStatus
import com.voyager.tourism.util.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserMapperTest {

    private val mapper = UserMapper()

    @Test
    fun `toDomain maps dto fields`() {
        val dto = TestFixtures.userDto()
        val u = mapper.toDomain(dto)
        assertEquals("42", u.id)
        assertEquals(UserRole.TRAVELER.value, u.role)
        assertEquals(UserStatus.ACTIVE.value, u.status)
        assertEquals(setOf("museums"), u.interests)
    }

    @Test
    fun `toDto round trip id`() {
        val domain = TestFixtures.domainUser()
        val back = mapper.toDto(domain)
        assertEquals(42L, back.id)
    }

    @Test
    fun `toEntity from dto sets verified from active status`() {
        val dto = TestFixtures.userDto()
        val e = mapper.toEntity(dto)
        assertEquals("42", e.id)
        assertTrue(e.isVerified)
    }

    @Test
    fun `toEntity from domain respects status`() {
        val domain = TestFixtures.domainUser().copy(status = UserStatus.PENDING.value)
        val e = mapper.toEntity(domain)
        assertFalse(e.isVerified)
    }

    @Test
    fun `entityToDomain maps verification`() {
        val entity = UserEntity(
            id = "1",
            email = "a@b.c",
            username = "u",
            firstName = "f",
            lastName = "l",
            avatar = null,
            preferences = null,
            isVerified = true,
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )
        val u = mapper.entityToDomain(entity)
        assertEquals(UserStatus.ACTIVE.value, u.status)
    }

    @Test
    fun `toUserUpdateDto maps profile fields`() {
        val u = TestFixtures.domainUser().copy(
            firstName = "A",
            lastName = "B",
            phoneNumber = "+1",
            bio = "hi",
            interests = setOf("x"),
        )
        val dto = mapper.toUserUpdateDto(u)
        assertEquals("A", dto.firstName)
        assertEquals("B", dto.lastName)
        assertEquals("+1", dto.phoneNumber)
        assertEquals("hi", dto.bio)
        assertEquals(listOf("x"), dto.interests)
    }
}
