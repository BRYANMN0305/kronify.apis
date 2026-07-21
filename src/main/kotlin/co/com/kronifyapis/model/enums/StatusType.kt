/**
 * Enum que define los estados de una invitación de empleado.
 * PENDING: el invitado aún no ha aceptado
 * ACCEPTED: el invitado aceptó y ya es empleado
 * EXPIRED: la invitación venció
 * CANCELLED: el negocio la canceló
 */
package co.com.kronifyapis.model.enums

enum class StatusType {
    PENDING,
    ACCEPTED,
    EXPIRED,
    CANCELLED
}
