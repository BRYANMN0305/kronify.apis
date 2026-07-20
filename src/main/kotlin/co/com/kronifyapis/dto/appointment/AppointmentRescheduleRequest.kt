package co.com.kronifyapis.dto.appointment

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class AppointmentRescheduleRequest(

    @field:NotNull
    @field:Future
    val startAt: LocalDateTime
)
