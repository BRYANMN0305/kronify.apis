package co.com.kronifyapis.dto.appointment

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

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
