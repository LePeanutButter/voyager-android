package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanRequest
import com.voyager.tourism.domain.repository.TravelRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for creating travel plans
 * Encapsulates the business logic for travel plan creation
 */
class CreateTravelPlanUseCase @Inject constructor(
    private val travelRepository: TravelRepository
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Create a new travel plan with validation
     * @param title Travel plan title
     * @param destination Destination location
     * @param origin Origin location (optional)
     * @param startDate Start date (yyyy-MM-dd format)
     * @param endDate End date (yyyy-MM-dd format)
     * @param budget Estimated budget (optional)
     * @param travelers Number of travelers
     * @param description Travel plan description (optional)
     * @return Result containing created TravelPlanDto or error
     */
    suspend operator fun invoke(
        title: String,
        destination: String,
        origin: String?,
        startDate: String,
        endDate: String,
        budget: Double?,
        travelers: Int,
        description: String?
    ): Result<TravelPlanDto> {
        // Validate required fields
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("El título es requerido"))
        }
        
        if (destination.isBlank()) {
            return Result.failure(IllegalArgumentException("El destino es requerido"))
        }
        
        if (startDate.isBlank()) {
            return Result.failure(IllegalArgumentException("La fecha de inicio es requerida"))
        }
        
        if (endDate.isBlank()) {
            return Result.failure(IllegalArgumentException("La fecha de fin es requerida"))
        }
        
        // Validate date format and logic
        val startDateObj = try {
            dateFormat.parse(startDate)
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Formato de fecha de inicio inválido. Use yyyy-MM-dd"))
        }
        
        val endDateObj = try {
            dateFormat.parse(endDate)
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Formato de fecha de fin inválido. Use yyyy-MM-dd"))
        }
        
        // Validate date range
        if (startDateObj != null && endDateObj != null) {
            if (endDateObj.before(startDateObj)) {
                return Result.failure(IllegalArgumentException("El rango de fechas es inválido"))
            }
            
            // Check if dates are in the past
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            if (startDateObj.before(today)) {
                return Result.failure(IllegalArgumentException("La fecha de inicio no puede ser en el pasado"))
            }
        }
        
        // Validate number of travelers
        if (travelers < 1) {
            return Result.failure(IllegalArgumentException("El número de viajeros debe ser al menos 1"))
        }
        
        // Validate budget if provided
        budget?.let {
            if (it < 0) {
                return Result.failure(IllegalArgumentException("El presupuesto no puede ser negativo"))
            }
        }
        
        return try {
            val request = TravelPlanRequest(
                title = title.trim(),
                destinationLocation = destination.trim(),
                originLocation = origin?.trim()?.takeIf { it.isNotBlank() },
                startDate = startDate,
                endDate = endDate,
                estimatedBudget = budget,
                numberOfTravelers = travelers,
                description = description?.trim()?.takeIf { it.isNotBlank() }
            )
            
            travelRepository.createTravelPlan(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
