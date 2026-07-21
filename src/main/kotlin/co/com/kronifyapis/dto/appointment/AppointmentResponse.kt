package co.com.kronifyapis.dto.appointment

import co.com.kronifyapis.model.enums.AppointmentOrigin
import co.com.kronifyapis.model.enums.AppointmentStatus
import java.time.LocalDateTime

/**
 * DTO que devuelve la información completa de una cita.
 */

data class AppointmentResponse(
    val appointmentId: Long,
    val businessId: Long,
    val serviceId: Long,
    val serviceName: String,
    val serviceDurationMinutes: Int,
    val employeeId: Long,
    val employeeName: String,
    val customerId: Long?,
    val customerName: String?,
    val customerPhone: String?,
    val customerEmail: String?,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val status: AppointmentStatus,
    val origin: AppointmentOrigin,
    val createdAt: LocalDateTime
)
