package com.voyager.tourism.domain.usecase.behavior

import com.voyager.tourism.data.dto.ImplicitPreferenceUpdate
import com.voyager.tourism.domain.repository.BehaviorAnalysisRepository
import javax.inject.Inject

/**
 * Use case for analyzing user behavior patterns
 * Encapsulates business logic for behavior analysis and preference updates
 */
class AnalyzeUserBehaviorUseCase @Inject constructor(
    private val behaviorAnalysisRepository: BehaviorAnalysisRepository
) {
    suspend operator fun invoke(
        userId: String,
        analysisPeriodDays: Int = 7,
        includePatterns: Boolean = true,
        includePreferenceUpdates: Boolean = true
    ): Result<ImplicitPreferenceUpdate> {
        // Validate input
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }
        
        if (analysisPeriodDays < 1 || analysisPeriodDays > 365) {
            return Result.failure(IllegalArgumentException("Analysis period must be between 1 and 365 days"))
        }
        
        return behaviorAnalysisRepository.analyzeUserBehavior(
            userId = userId,
            analysisPeriodDays = analysisPeriodDays,
            includePatterns = includePatterns,
            includePreferenceUpdates = includePreferenceUpdates
        )
    }
}
