package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class ScheduleBlockRequest(
    @field:NotNull
    val startAt: LocalDateTime,

    @field:NotNull
    val endAt: LocalDateTime,

    val reason: String? = null
)
