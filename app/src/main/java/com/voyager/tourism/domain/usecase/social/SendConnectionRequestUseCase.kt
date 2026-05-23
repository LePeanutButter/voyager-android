package com.voyager.tourism.domain.usecase.social

import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.domain.repository.SocialRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for sending a connection request to another traveler
 * 
 * This use case handles the business logic for sending connection requests
 * and validates that duplicate requests are not sent.
 */
@Singleton
class SendConnectionRequestUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    /**
     * Send a connection request to another traveler
     * 
     * @param recipientId The ID of the traveler to send the request to
     * @param message Optional message to include with the request
     * @param token Authentication token
     * @return Result containing the created connection request or error
     */
    suspend operator fun invoke(
        recipientId: Long,
        message: String? = null,
        token: String
    ): Result<ConnectionRequestDto> {
        return try {
            // Check if there's already a pending request
            val sentRequests = socialRepository.getSentRequests(token)
            val existingRequest = sentRequests.find { 
                it.recipientId == recipientId && it.status == "PENDING" 
            }
            
            if (existingRequest != null) {
                return Result.failure(
                    IllegalStateException("A connection request is already pending with this traveler")
                )
            }
            
            val request = SendConnectionRequestDto(
                recipientId = recipientId,
                message = message
            )
            
            val connectionRequest = socialRepository.sendConnectionRequest(request, token)
            Result.success(connectionRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
