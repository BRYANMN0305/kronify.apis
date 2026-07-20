package co.com.kronifyapis.dto.appointment

import jakarta.validation.constraints.NotNull

data class AppointmentStatusUpdateRequest(

    @field:NotNull
    val status: AppointmentStatus,

    val cancellationReason: String? = null
)
