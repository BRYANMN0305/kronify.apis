package co.com.kronifyapis.dto.availability

import java.time.LocalDateTime

data class TimeSlotResponse(
    val employeeId: Long,
    val employeeName: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime
)