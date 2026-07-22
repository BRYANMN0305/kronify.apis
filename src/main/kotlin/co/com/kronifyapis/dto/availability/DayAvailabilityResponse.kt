package co.com.kronifyapis.dto.availability

import java.time.LocalDate

/**
 * DTO que devuelve los horarios disponibles de un servicio para una fecha específica,
 * organizados en bloques de tiempo disponibles.
 */

data class DayAvailabilityResponse(
    val date: LocalDate,
    val serviceId: Long,
    val serviceDurationMinutes: Int,
    val slots: List<TimeSlotResponse>
)