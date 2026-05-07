package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.UserApiService
import com.voyager.tourism.data.database.dao.TripDao
import com.voyager.tourism.data.database.dao.UserDao
import com.voyager.tourism.data.dto.ApiResponse
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.mapper.UserMapper
import com.voyager.tourism.domain.model.BudgetRange
import com.voyager.tourism.domain.model.TravelInterest
import com.voyager.tourism.domain.model.User
import com.voyager.tourism.domain.model.UserPreferences
import com.voyager.tourism.domain.repository.VoyagerAiRepository
import com.voyager.tourism.util.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class UserRepositoryImplTest {

    private val userApi = mockk<UserApiService>()
    private val voyagerAi = mockk<VoyagerAiRepository>()
    private val userDao = mockk<UserDao>(relaxed = true)
    private val tripDao = mockk<TripDao>(relaxed = true)
    private val userMapper = UserMapper()
    private val prefs = mockk<PreferencesManager>()
    private lateinit var repo: UserRepositoryImpl

    private fun <T> apiOk(data: T?, status: Int = 200, msg: String = "OK") =
        ApiResponse("t", status, msg, data, null, null)

    @Before
    fun setup() {
        repo = UserRepositoryImpl(userApi, voyagerAi, userDao, tripDao, userMapper, prefs)
    }

    @Test
    fun `getCurrentUser no token returns null`() = runTest {
        every { prefs.getAuthToken() } returns null
        assertEquals(null, repo.getCurrentUser().getOrNull())
    }

    @Test
    fun `getCurrentUser with token uses dao when user id missing`() = runTest {
        every { prefs.getAuthToken() } returns "j"
        every { prefs.getCurrentUserId() } returns null
        val entity = userMapper.toEntity(TestFixtures.userDto())
        coEvery { userDao.getCurrentUser() } returns entity
        val u = repo.getCurrentUser().getOrNull()
        assertEquals("42", u?.id)
    }

    @Test
    fun `getCurrentUser fetches remote when user id present`() = runTest {
        every { prefs.getAuthToken() } returns "j"
        every { prefs.getCurrentUserId() } returns "42"
        val dto = TestFixtures.userDto()
        coEvery { userApi.getUserById(42L) } returns apiOk(dto)
        val u = repo.getCurrentUser().getOrNull()
        assertEquals(dto.email, u?.email)
        coVerify { userDao.insertUser(any()) }
    }

    @Test
    fun `getUserById invalid id`() = runTest {
        assertTrue(repo.getUserById("abc").isFailure)
    }

    @Test
    fun `getUserById success`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { userApi.getUserById(42L) } returns apiOk(dto)
        assertTrue(repo.getUserById("42").isSuccess)
    }

    @Test
    fun `authenticate persists token and user`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { userApi.loginUser(any()) } returns apiOk(dto)
        every { prefs.saveAuthToken(any()) } returns Unit
        every { prefs.saveCurrentUserId(any()) } returns Unit
        val r = repo.authenticate("e", "p")
        assertTrue(r.isSuccess)
    }

    @Test
    fun `register success`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { userApi.registerUser(any()) } returns apiOk(dto, 201)
        every { prefs.saveCurrentUserId(any()) } returns Unit
        assertTrue(repo.register("e", "p", "u", "F", "L").isSuccess)
    }

    @Test
    fun `updateUser domain success`() = runTest {
        val domain = TestFixtures.domainUser()
        val dto = TestFixtures.userDto()
        coEvery { userApi.updateUser(42L, any()) } returns apiOk(dto)
        assertTrue(repo.updateUser(domain).isSuccess)
    }

    @Test
    fun `updateUser domain invalid id`() = runTest {
        assertTrue(repo.updateUser(TestFixtures.domainUser().copy(id = "x")).isFailure)
    }

    @Test
    fun `updateUser dto overload requires auth`() = runTest {
        every { prefs.getAuthToken() } returns null
        assertTrue(
            repo.updateUser("42", "A", "B", "bio", null, emptyList()).isFailure,
        )
    }

    @Test
    fun `updateUser dto overload success`() = runTest {
        every { prefs.getAuthToken() } returns "t"
        val dto = TestFixtures.userDto()
        coEvery { userApi.updateUser(42L, any()) } returns apiOk(dto)
        assertTrue(
            repo.updateUser("42", "A", "B", "bio", null, listOf("art")).isSuccess,
        )
    }

    @Test
    fun `updateUserPreferences refreshes profile`() = runTest {
        coEvery { voyagerAi.postUserPreferences(eq("42"), any()) } returns Response.success("{}".toResponseBody())
        val dto = TestFixtures.userDto()
        coEvery { userApi.getUserById(42L) } returns apiOk(dto)
        val prefsModel = UserPreferences(
            interests = listOf(TravelInterest.ART),
            budgetRange = BudgetRange.MEDIUM,
        )
        assertTrue(repo.updateUserPreferences("42", prefsModel).isSuccess)
        coVerify { userDao.updateUser(any()) }
    }

    @Test
    fun `logout clears trips`() = runTest {
        every { prefs.getCurrentUserId() } returns "1"
        every { prefs.clearAuthData() } returns Unit
        assertTrue(repo.logout().isSuccess)
        coVerify { tripDao.deleteAllUserTrips("1") }
    }

    @Test
    fun `deleteUser clears local`() = runTest {
        coEvery { userApi.deleteUser(42L) } returns apiOk(null)
        every { prefs.clearAuthData() } returns Unit
        assertTrue(repo.deleteUser("42").isSuccess)
        coVerify { userDao.deleteUserById("42") }
    }

    @Test
    fun `isUserAuthenticated reflects flow`() = runTest {
        every { prefs.authTokenFlow } returns MutableStateFlow<String?>("jwt")
        assertTrue(repo.isUserAuthenticated().first())
    }
}
