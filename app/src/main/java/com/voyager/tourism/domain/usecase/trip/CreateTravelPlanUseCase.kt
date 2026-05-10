package com.voyager.tourism.domain.usecase.trip

import com.voyager.tourism.data.dto.TravelPlanDto
import com.voyager.tourism.data.dto.TravelPlanRequest
import com.voyager.tourism.domain.repository.TravelRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Parámetros para crear un plan de viaje (agrupa campos para cumplir límites de aridad en análisis estático).
 */
data class CreateTravelPlanParams(
    val title: String,
    val destination: String,
    val origin: String?,
    val startDate: String,
    val endDate: String,
    val budget: Double?,
    val travelers: Int,
    val description: String?,
)

/**
 * Use case for creating travel plans
 * Encapsulates the business logic for travel plan creation
 */
class CreateTravelPlanUseCase @Inject constructor(
    private val travelRepository: TravelRepository
) {

    private companion object {
        const val COP_MIN = 50_000L
        const val COP_STEP = 50L
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Create a new travel plan with validation
     * @return Result containing created TravelPlanDto or error
     */
    suspend operator fun invoke(params: CreateTravelPlanParams): Result<TravelPlanDto> {
        val title = params.title
        val destination = params.destination
        val origin = params.origin
        val startDate = params.startDate
        val endDate = params.endDate
        val budget = params.budget
        val travelers = params.travelers
        val description = params.description

        validateRequired(title = title, destination = destination, startDate = startDate, endDate = endDate)
            ?.let { return Result.failure(it) }

        val startDateObj = parseRequiredDate(startDate)
            ?: return Result.failure(IllegalArgumentException("Formato de fecha de inicio inválido. Use yyyy-MM-dd"))
        val endDateObj = parseRequiredDate(endDate)
            ?: return Result.failure(IllegalArgumentException("Formato de fecha de fin inválido. Use yyyy-MM-dd"))

        validateDates(startDateObj = startDateObj, endDateObj = endDateObj)
            ?.let { return Result.failure(it) }

        validateTravelers(travelers)?.let { return Result.failure(it) }
        validateCopBudget(budget)?.let { return Result.failure(it) }

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

    private fun validateRequired(
        title: String,
        destination: String,
        startDate: String,
        endDate: String,
    ): Exception? {
        if (title.isBlank()) return IllegalArgumentException("El título es requerido")
        if (destination.isBlank()) return IllegalArgumentException("El destino es requerido")
        if (startDate.isBlank()) return IllegalArgumentException("La fecha de inicio es requerida")
        if (endDate.isBlank()) return IllegalArgumentException("La fecha de fin es requerida")
        return null
    }

    private fun parseRequiredDate(value: String): Date? {
        val day = value.trim().take(10)
        return try {
            dateFormat.parse(day)
        } catch (_: Exception) {
            null
        }
    }

    private fun validateDates(startDateObj: Date, endDateObj: Date): Exception? {
        if (endDateObj.before(startDateObj)) {
            return IllegalArgumentException("El rango de fechas es inválido")
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        if (startDateObj.before(today)) {
            return IllegalArgumentException("La fecha de inicio no puede ser en el pasado")
        }
        return null
    }

    private fun validateTravelers(travelers: Int): Exception? {
        if (travelers < 1) return IllegalArgumentException("El número de viajeros debe ser al menos 1")
        return null
    }

    private fun validateCopBudget(budget: Double?): Exception? {
        if (budget == null) {
            return IllegalArgumentException(
                "Indica un presupuesto mínimo de $COP_MIN COP en múltiplos de $COP_STEP.",
            )
        }
        val cop = kotlin.math.round(budget).toLong()
        if (cop < COP_MIN) {
            return IllegalArgumentException("El presupuesto mínimo es $COP_MIN COP.")
        }
        if (cop % COP_STEP != 0L) {
            return IllegalArgumentException("El presupuesto debe ser múltiplo de $COP_STEP COP.")
        }
        return null
    }
}
