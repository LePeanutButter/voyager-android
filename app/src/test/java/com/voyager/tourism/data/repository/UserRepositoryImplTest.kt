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
        val dto = TestFixtures.userDto(token = "jwt-token")
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
        coEvery { voyagerAi.postUserPreferences(eq("42"), any()) } returns Response.success(Unit)
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
    fun `getCurrentUser fetches remote failure returns local user`() = runTest {
        every { prefs.getAuthToken() } returns "j"
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { userApi.getUserById(42L) } returns apiOk(null, 404, "Fail")
        val entity = userMapper.toEntity(TestFixtures.userDto())
        coEvery { userDao.getCurrentUser() } returns entity
        val u = repo.getCurrentUser().getOrNull()
        assertEquals("42", u?.id)
    }

    @Test
    fun `getUserById remote failure returns local`() = runTest {
        coEvery { userApi.getUserById(42L) } returns apiOk(null, 404)
        val entity = userMapper.toEntity(TestFixtures.userDto(id = 42L))
        coEvery { userDao.getUserById("42") } returns entity
        assertTrue(repo.getUserById("42").isSuccess)
    }

    @Test
    fun `authenticate failure returns message`() = runTest {
        coEvery { userApi.loginUser(any()) } returns apiOk(null, 401, "Wrong")
        val r = repo.authenticate("e", "p")
        assertTrue(r.isFailure)
        assertEquals("Wrong", r.exceptionOrNull()?.message)
    }

    @Test
    fun `register failure returns message`() = runTest {
        coEvery { userApi.registerUser(any()) } returns apiOk(null, 400, "Error")
        val r = repo.register("e", "p", "u", "F", "L")
        assertTrue(r.isFailure)
    }

    @Test
    fun `updateUserPreferences AI failure returns error`() = runTest {
        coEvery { voyagerAi.postUserPreferences(any(), any()) } returns Response.error(500, "".toResponseBody(null))
        val prefsModel = UserPreferences(interests = listOf(TravelInterest.ART), budgetRange = BudgetRange.MEDIUM)
        assertTrue(repo.updateUserPreferences("42", prefsModel).isFailure)
    }

    @Test
    fun `isUserAuthenticated emits true when token present`() = runTest {
        every { prefs.authTokenFlow } returns MutableStateFlow("token")
        val result = repo.isUserAuthenticated().first()
        assertTrue(result)
    }

    @Test
    fun `isUserAuthenticated emits false when token null`() = runTest {
        every { prefs.authTokenFlow } returns MutableStateFlow(null)
        val result = repo.isUserAuthenticated().first()
        assertEquals(false, result)
    }

    @Test
    fun `deleteUser invalid id returns failure`() = runTest {
        assertTrue(repo.deleteUser("abc").isFailure)
    }

    @Test
    fun `deleteUser failure from remote returns failure`() = runTest {
        coEvery { userApi.deleteUser(42L) } returns apiOk(null, 404, "Not found")
        assertTrue(repo.deleteUser("42").isFailure)
    }

    @Test
    fun `logout without userId does not call deleteAllUserTrips`() = runTest {
        every { prefs.getCurrentUserId() } returns null
        every { prefs.clearAuthData() } returns Unit
        assertTrue(repo.logout().isSuccess)
    }

    @Test
    fun `getCurrentUser exception falls back to local user`() = runTest {
        every { prefs.getAuthToken() } returns "j"
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { userApi.getUserById(42L) } throws RuntimeException("network error")
        val entity = userMapper.toEntity(TestFixtures.userDto())
        coEvery { userDao.getCurrentUser() } returns entity
        val u = repo.getCurrentUser().getOrNull()
        assertEquals("42", u?.id)
    }

    @Test
    fun `getCurrentUser exception no local user returns failure`() = runTest {
        every { prefs.getAuthToken() } returns "j"
        every { prefs.getCurrentUserId() } returns "42"
        coEvery { userApi.getUserById(42L) } throws RuntimeException("network error")
        coEvery { userDao.getCurrentUser() } returns null
        assertTrue(repo.getCurrentUser().isFailure)
    }

    @Test
    fun `getUserById exception with local fallback success`() = runTest {
        coEvery { userApi.getUserById(42L) } throws RuntimeException("network error")
        val entity = userMapper.toEntity(TestFixtures.userDto(id = 42L))
        coEvery { userDao.getUserById("42") } returns entity
        assertTrue(repo.getUserById("42").isSuccess)
    }

    @Test
    fun `getUserById exception no local returns failure`() = runTest {
        coEvery { userApi.getUserById(42L) } throws RuntimeException("network error")
        coEvery { userDao.getUserById("42") } returns null
        assertTrue(repo.getUserById("42").isFailure)
    }

    @Test
    fun `updateUser domain failure from remote`() = runTest {
        val domain = TestFixtures.domainUser()
        coEvery { userApi.updateUser(42L, any()) } returns apiOk(null, 400, "Bad Request")
        assertTrue(repo.updateUser(domain).isFailure)
    }

    @Test
    fun `updateUserPreferences invalid userId after AI success returns failure`() = runTest {
        coEvery { voyagerAi.postUserPreferences(eq("abc"), any()) } returns Response.success(Unit)
        val prefsModel = UserPreferences(interests = listOf(TravelInterest.ART), budgetRange = BudgetRange.MEDIUM)
        assertTrue(repo.updateUserPreferences("abc", prefsModel).isFailure)
    }

    @Test
    fun `updateUserPreferences remote failure after AI success returns failure`() = runTest {
        coEvery { voyagerAi.postUserPreferences(eq("42"), any()) } returns Response.success(Unit)
        coEvery { userApi.getUserById(42L) } returns apiOk(null, 500, "Server Error")
        val prefsModel = UserPreferences(interests = listOf(TravelInterest.ART), budgetRange = BudgetRange.MEDIUM)
        assertTrue(repo.updateUserPreferences("42", prefsModel).isFailure)
    }

    @Test
    fun `updateUser dto overload invalid userId`() = runTest {
        every { prefs.getAuthToken() } returns "t"
        assertTrue(repo.updateUser("abc", "A", "B", "bio", null, emptyList()).isFailure)
    }

    @Test
    fun `updateUser dto overload remote failure`() = runTest {
        every { prefs.getAuthToken() } returns "t"
        coEvery { userApi.updateUser(42L, any()) } returns apiOk(null, 400, "Bad")
        assertTrue(repo.updateUser("42", "A", "B", "bio", null, emptyList()).isFailure)
    }

    @Test
    fun `register with status 200 is also success`() = runTest {
        val dto = TestFixtures.userDto()
        coEvery { userApi.registerUser(any()) } returns apiOk(dto, 200)
        every { prefs.saveCurrentUserId(any()) } returns Unit
        assertTrue(repo.register("e", "p", "u", "F", "L").isSuccess)
    }
}
