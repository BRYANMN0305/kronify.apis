/**
 * Enum que define los posibles estados de una cita.
 * PENDING: pendiente de confirmar
 * CONFIRMED: confirmada por el negocio
 * CANCELLED: cancelada
 * COMPLETED: completada (asistió)
 * NO_SHOW: el cliente no asistió
 */
package co.com.kronifyapis.model.enums

enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    NO_SHOW
}
