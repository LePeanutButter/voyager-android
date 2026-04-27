package com.voyager.tourism.domain.repository

import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.data.dto.TravelerMatchDto

/**
 * Repository interface for social features
 * 
 * This interface defines the contract for social-related data operations
 * including traveler matching and connection requests.
 */
interface SocialRepository {
    /**
     * Get compatible travelers for a specific travel plan
     */
    suspend fun getCompatibleTravelers(travelPlanId: String, token: String): List<TravelerMatchDto>
    
    /**
     * Send a connection request to another traveler
     */
    suspend fun sendConnectionRequest(request: SendConnectionRequestDto, token: String): ConnectionRequestDto
    
    /**
     * Accept a connection request
     */
    suspend fun acceptConnectionRequest(requestId: String, token: String): ConnectionRequestDto
    
    /**
     * Reject a connection request
     */
    suspend fun rejectConnectionRequest(requestId: String, token: String): ConnectionRequestDto
    
    /**
     * Get pending connection requests received by the user
     */
    suspend fun getPendingRequests(token: String): List<ConnectionRequestDto>
    
    /**
     * Get connection requests sent by the user
     */
    suspend fun getSentRequests(token: String): List<ConnectionRequestDto>
}
