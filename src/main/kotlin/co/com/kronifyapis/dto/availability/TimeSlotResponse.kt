package co.com.kronifyapis.dto.availability

import java.time.LocalDateTime

/**
 * DTO que representa un bloque de tiempo disponible para agendar una cita,
 * asociado a un empleado específico.
 */

data class TimeSlotResponse(
    val employeeId: Long,
    val employeeName: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime
)