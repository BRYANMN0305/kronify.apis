package co.com.kronifyapis.service

import java.time.LocalDateTime

/**
 * Eventos de cita que generan correos informativos.
 */
enum class AppointmentNotificationType {
    PUBLIC_BOOKING_PENDING,
    BUSINESS_BOOKING_CONFIRMED,
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_CANCELLED_BY_BUSINESS,
    APPOINTMENT_CANCELLED_BY_CLIENT,
    APPOINTMENT_RESCHEDULED,
    APPOINTMENT_REMINDER,
    APPOINTMENT_COMPLETED,
    APPOINTMENT_NO_SHOW,
    APPOINTMENT_AUTO_CANCELLED
}

/**
 * Copia inmutable de los datos de una cita necesarios para redactar un correo.
 *
 * No contiene entidades JPA: así el envío asíncrono puede ejecutarse después
 * del commit sin depender de sesiones de Hibernate ya cerradas.
 */
data class AppointmentNotificationEvent(
    val type: AppointmentNotificationType,
    val appointmentId: Long,
    val businessName: String,
    val serviceName: String,
    val employeeName: String,
    val employeeEmail: String?,
    val customerName: String,
    val customerEmail: String?,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val cancellationReason: String?
)
