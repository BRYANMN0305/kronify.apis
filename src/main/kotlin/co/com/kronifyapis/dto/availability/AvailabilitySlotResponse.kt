package co.com.kronifyapis.dto.availability

import java.time.LocalDateTime

data class AvailabilitySlotResponse(
    val employeeId: Long,
    val serviceId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime
)
