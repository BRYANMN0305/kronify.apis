/**
 * Enum que indica desde dónde se creó la cita.
 * PUBLIC: creada por un cliente desde la página pública
 * PRIVATE: creada por el negocio desde el panel interno
 * ADMIN: creada por un administrador
 */
package co.com.kronifyapis.model.enums

enum class AppointmentOrigin {
    PUBLIC,
    PRIVATE,
    ADMIN
}
