package com.voyager.tourism.domain.usecase.social

import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.domain.repository.SocialRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for responding to connection requests (accept or reject)
 * 
 * This use case handles the business logic for accepting or rejecting
 * connection requests from other travelers.
 */
@Singleton
class RespondToConnectionRequestUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    /**
     * Accept a connection request
     * 
     * @param requestId The ID of the connection request to accept
     * @param token Authentication token
     * @return Result containing the updated connection request or error
     */
    suspend fun acceptRequest(
        requestId: String,
        token: String
    ): Result<ConnectionRequestDto> {
        return try {
            val connectionRequest = socialRepository.acceptConnectionRequest(requestId, token)
            Result.success(connectionRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reject a connection request
     * 
     * @param requestId The ID of the connection request to reject
     * @param token Authentication token
     * @return Result containing the updated connection request or error
     */
    suspend fun rejectRequest(
        requestId: String,
        token: String
    ): Result<ConnectionRequestDto> {
        return try {
            val connectionRequest = socialRepository.rejectConnectionRequest(requestId, token)
            Result.success(connectionRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
