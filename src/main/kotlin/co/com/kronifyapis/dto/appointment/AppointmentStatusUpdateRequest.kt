package co.com.kronifyapis.dto.appointment

import co.com.kronifyapis.model.enums.AppointmentStatus
import jakarta.validation.constraints.NotNull

/**
 * DTO que recibe el nuevo estado de una cita y una razón opcional
 */

data class AppointmentStatusUpdateRequest(

    @field:NotNull
    val status: AppointmentStatus,

    val cancellationReason: String? = null
)
