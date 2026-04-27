package com.voyager.tourism.data.repository

import com.voyager.tourism.data.api.TourismApiService
import com.voyager.tourism.data.dto.ConnectionRequestDto
import com.voyager.tourism.data.dto.SendConnectionRequestDto
import com.voyager.tourism.data.dto.TravelerMatchDto
import com.voyager.tourism.domain.repository.SocialRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SocialRepository interface
 * 
 * This repository handles social-related data operations by communicating
 * with the backend API through the TourismApiService.
 */
@Singleton
class SocialRepositoryImpl @Inject constructor(
    private val apiService: TourismApiService
) : SocialRepository {
    
    override suspend fun getCompatibleTravelers(
        travelPlanId: String,
        token: String
    ): List<TravelerMatchDto> {
        val response = apiService.getCompatibleTravelers(travelPlanId, token)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get compatible travelers: ${response.code()}")
        }
    }
    
    override suspend fun sendConnectionRequest(
        request: SendConnectionRequestDto,
        token: String
    ): ConnectionRequestDto {
        val response = apiService.sendConnectionRequest(request, token)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to send connection request: ${response.code()}")
        }
    }
    
    override suspend fun acceptConnectionRequest(
        requestId: String,
        token: String
    ): ConnectionRequestDto {
        val response = apiService.acceptConnectionRequest(requestId, token)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to accept connection request: ${response.code()}")
        }
    }
    
    override suspend fun rejectConnectionRequest(
        requestId: String,
        token: String
    ): ConnectionRequestDto {
        val response = apiService.rejectConnectionRequest(requestId, token)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to reject connection request: ${response.code()}")
        }
    }
    
    override suspend fun getPendingRequests(token: String): List<ConnectionRequestDto> {
        val response = apiService.getPendingRequests(token)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get pending requests: ${response.code()}")
        }
    }
    
    override suspend fun getSentRequests(token: String): List<ConnectionRequestDto> {
        val response = apiService.getSentRequests(token)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to get sent requests: ${response.code()}")
        }
    }
}
