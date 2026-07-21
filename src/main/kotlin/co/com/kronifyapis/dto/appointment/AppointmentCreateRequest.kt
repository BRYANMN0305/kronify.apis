package co.com.kronifyapis.dto.appointment


import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * DTO que recibe los datos para crear una nueva cita.
 * Si el cliente no está registrado, se envían sus datos en los campos
 * customerName, customerLastName, customerPhone y customerEmail.
 */

data class AppointmentCreateRequest(

    val businessId: Long? = null,

    @field:NotNull
    val serviceId: Long,

    @field:NotNull
    val employeeId: Long,

    @field:NotNull
    @field:Future
    val startAt: LocalDateTime,

    val customerId: Long? = null,

    val customerName: String? = null,

    val customerLastName: String? = null,

    val customerPhone: String? = null,

    val customerEmail: String? = null
)
