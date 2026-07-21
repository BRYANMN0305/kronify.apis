package co.com.kronifyapis.dto.appointment


import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * DTO que recibe la nueva fecha y hora para reprogramar una cita existente.
 */

data class AppointmentRescheduleRequest(

    @field:NotNull
    @field:Future
    val startAt: LocalDateTime
)
