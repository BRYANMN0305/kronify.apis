package co.com.kronifyapis.dto.appointment

import java.time.LocalDateTime

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
