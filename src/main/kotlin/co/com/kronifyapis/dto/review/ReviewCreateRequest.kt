package co.com.kronifyapis.dto.review

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ReviewCreateRequest(
    @field:NotNull
    val appointmentId: Long,

    @field:Min(1)
    @field:Max(5)
    val rating: Int,

    @field:Size(max = 1000)
    val comment: String? = null
)
