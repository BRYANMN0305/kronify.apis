package co.com.kronifyapis.dto.appointment

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
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

    @field:Size(max = 120)
    val customerName: String? = null,

    @field:Size(max = 120)
    val customerLastName: String? = null,

    @field:Size(max = 30)
    val customerPhone: String? = null,

    @field:Size(max = 160)
    val customerEmail: String? = null
)
