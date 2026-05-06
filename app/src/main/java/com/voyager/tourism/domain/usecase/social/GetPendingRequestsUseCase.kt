package com.voyager.tourism.domain.usecase.social

import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.domain.repository.SocialRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for getting pending connection requests
 * 
 * This use case handles retrieving pending connection requests
 * that the current user has received from other travelers.
 */
@Singleton
class GetPendingRequestsUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    /**
     * Get all pending connection requests for the current user
     * 
     * @param token Authentication token
     * @return Result containing list of pending connection requests or error
     */
    suspend operator fun invoke(token: String): Result<List<ConnectionRequestDto>> {
        return try {
            val requests = socialRepository.getPendingRequests(token)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
