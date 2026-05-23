package com.voyager.tourism.domain.usecase.social

import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.domain.repository.SocialRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for finding compatible travelers for a specific travel plan
 * 
 * This use case handles the business logic for finding travelers with similar
 * destinations and compatible travel dates.
 */
@Singleton
class GetCompatibleTravelersUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    /**
     * Find compatible travelers for a given travel plan
     * 
     * @param travelPlanId The ID of the travel plan to find matches for
     * @param token Authentication token
     * @return Result containing list of compatible travelers or error
     */
    suspend operator fun invoke(
        travelPlanId: String,
        token: String
    ): Result<List<TravelerMatchDto>> {
        return try {
            val travelers = socialRepository.getCompatibleTravelers(travelPlanId, token)
            Result.success(travelers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
