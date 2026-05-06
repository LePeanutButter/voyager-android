package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.TravelPlanApiService
import com.voyager.tourism.data.database.dao.TripDao
import com.voyager.tourism.data.dto.TravelPlanStatus
import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.mapper.TripMapper
import com.voyager.tourism.domain.model.Trip
import com.voyager.tourism.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Trips stored locally in Room plus **travel-plans** endpoints from the Spring backend.
 */
@Singleton
class TripRepositoryImpl @Inject constructor(
    private val travelPlanApi: TravelPlanApiService,
    private val tripDao: TripDao,
    private val tripMapper: TripMapper,
    private val preferencesManager: PreferencesManager,
) : TripRepository {

    /**
     * Reads the opaque user id string currently stored after login.
     */
    private fun currentUserId(): String? = preferencesManager.getCurrentUserId()

    /** @see TripRepository.getUserTrips */
    override suspend fun getUserTrips(userId: String): Result<List<Trip>> {
        return try {
            if (preferencesManager.getAuthToken().isNullOrEmpty()) {
                Result.success(tripDao.getTripsByUserId(userId).map { tripMapper.toDomain(it) })
            } else {
                val uid = userId.toLongOrNull()
                    ?: return Result.failure(IllegalArgumentException("userId inválido"))
                val resp = travelPlanApi.getTravelPlansByUser(uid)
                if (resp.status == 200 && resp.data != null) {
                    val trips = resp.data.map { tripMapper.fromTravelPlanDto(it, userId) }
                    tripDao.deleteAllTrips()
                    tripDao.insertTrips(trips.map { tripMapper.toEntityFromTrip(it) })
                    Result.success(trips)
                } else {
                    Result.success(tripDao.getTripsByUserId(userId).map { tripMapper.toDomain(it) })
                }
            }
        } catch (e: Exception) {
            Result.success(tripDao.getTripsByUserId(userId).map { tripMapper.toDomain(it) })
        }
    }

    /** @see TripRepository.getTripById */
    override suspend fun getTripById(tripId: String): Result<Trip?> {
        return try {
            val id = tripId.toLongOrNull()
                ?: return Result.success(tripDao.getTripById(tripId)?.let { tripMapper.toDomain(it) })
            if (preferencesManager.getAuthToken().isNullOrEmpty()) {
                Result.success(tripDao.getTripById(tripId)?.let { tripMapper.toDomain(it) })
            } else {
                val resp = travelPlanApi.getTravelPlanById(id)
                if (resp.status == 200 && resp.data != null) {
                    val trip = tripMapper.fromTravelPlanDto(resp.data!!, currentUserId().orEmpty())
                    tripDao.insertTrip(tripMapper.toEntityFromTrip(trip))
                    Result.success(trip)
                } else {
                    Result.success(tripDao.getTripById(tripId)?.let { tripMapper.toDomain(it) })
                }
            }
        } catch (e: Exception) {
            Result.success(tripDao.getTripById(tripId)?.let { tripMapper.toDomain(it) })
        }
    }

    /** @see TripRepository.createTrip */
    override suspend fun createTrip(trip: Trip): Result<Trip> {
        return try {
            if (preferencesManager.getAuthToken().isNullOrEmpty()) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val body = tripMapper.toTravelPlanDto(trip)
                val resp = travelPlanApi.createTravelPlan(body)
                if ((resp.status == 200 || resp.status == 201) && resp.data != null) {
                    val created = tripMapper.fromTravelPlanDto(
                        resp.data!!,
                        currentUserId().orEmpty(),
                    )
                    tripDao.insertTrip(tripMapper.toEntityFromTrip(created))
                    Result.success(created)
                } else {
                    Result.failure(Exception(resp.message ?: "Failed to create trip"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see TripRepository.updateTrip */
    override suspend fun updateTrip(trip: Trip): Result<Trip> {
        return try {
            if (preferencesManager.getAuthToken().isNullOrEmpty()) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val id = trip.id.toLongOrNull()
                    ?: return Result.failure(IllegalArgumentException("id inválido"))
                val resp = travelPlanApi.updateTravelPlan(id, tripMapper.toTravelPlanDto(trip))
                if (resp.status == 200 && resp.data != null) {
                    val updated = tripMapper.fromTravelPlanDto(resp.data!!, currentUserId().orEmpty())
                    tripDao.updateTrip(tripMapper.toEntityFromTrip(updated))
                    Result.success(updated)
                } else {
                    Result.failure(Exception(resp.message ?: "Failed to update trip"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see TripRepository.deleteTrip */
    override suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            if (preferencesManager.getAuthToken().isNullOrEmpty()) {
                Result.failure(Exception("User not authenticated"))
            } else {
                val id = tripId.toLongOrNull()
                    ?: return Result.failure(IllegalArgumentException("id inválido"))
                val resp = travelPlanApi.deleteTravelPlan(id)
                if (resp.status == 200) {
                    tripDao.deleteTripById(tripId)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(resp.message ?: "Failed to delete trip"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** @see TripRepository.getActiveTrips */
    override suspend fun getActiveTrips(userId: String): Result<List<Trip>> =
        filterMyPlans(userId) { it.status == TravelPlanStatus.ACTIVE }

    /** @see TripRepository.getUpcomingTrips */
    override suspend fun getUpcomingTrips(userId: String): Result<List<Trip>> {
        val now = OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
        return filterMyPlans(userId) { plan ->
            val start = plan.startDate?.let { parseStart(it) } ?: return@filterMyPlans false
            start > now && plan.status != TravelPlanStatus.COMPLETED && plan.status != TravelPlanStatus.CANCELLED
        }
    }

    /** @see TripRepository.getCompletedTrips */
    override suspend fun getCompletedTrips(userId: String): Result<List<Trip>> =
        filterMyPlans(userId) { it.status == TravelPlanStatus.COMPLETED }

    /** @see TripRepository.searchTripsByDestination */
    override suspend fun searchTripsByDestination(userId: String, destination: String): Result<List<Trip>> =
        filterMyPlans(userId) {
            it.destinationLocation?.contains(destination, ignoreCase = true) == true
        }

    /** @see TripRepository.streamTripUpdates */
    override fun streamTripUpdates(tripId: String): Flow<Trip?> {
        return tripDao.streamTripById(tripId).map { entity ->
            entity?.let { tripMapper.toDomain(it) }
        }
    }

    /**
     * Filters the authenticated user's plans returned by [TravelPlanApiService.getMyTravelPlans].
     */
    private suspend fun filterMyPlans(
        userId: String,
        predicate: (com.voyager.tourism.data.dto.TravelPlanDto) -> Boolean,
    ): Result<List<Trip>> {
        return try {
            if (preferencesManager.getAuthToken().isNullOrEmpty()) {
                Result.success(emptyList())
            } else {
                val resp = travelPlanApi.getMyTravelPlans()
                if (resp.status == 200 && resp.data != null) {
                    val trips = resp.data!!.filter(predicate).map { tripMapper.fromTravelPlanDto(it, userId) }
                    Result.success(trips)
                } else {
                    Result.failure(Exception(resp.message ?: "Failed to load plans"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses a plan start timestamp from ISO-8601 text, tolerating date-only values.
     */
    private fun parseStart(iso: String): Long = try {
        OffsetDateTime.parse(iso).toInstant().toEpochMilli()
    } catch (_: Exception) {
        java.time.LocalDateTime.parse(iso).toInstant(ZoneOffset.UTC).toEpochMilli()
    }
}
