package co.com.kronifyapis.dto.appointment

/**
 * DTO que devuelve los datos del cliente autocompletados al agendar una cita,
 * basándose en el usuario autenticado.
 */

data class AppointmentAutofillResponse(
    val userId: Long,
    val customerId: Long?,
    val name: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String
)
