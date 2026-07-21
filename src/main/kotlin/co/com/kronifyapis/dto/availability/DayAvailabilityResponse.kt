package co.com.kronifyapis.dto.availability

import java.time.LocalDate

data class DayAvailabilityResponse(
    val date: LocalDate,
    val serviceId: Long,
    val serviceDurationMinutes: Int,
    val slots: List<TimeSlotResponse>
)