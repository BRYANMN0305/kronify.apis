package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

data class WeeklyScheduleRequest(
    @field:NotNull
    @field:Min(1)
    @field:Max(7)
    val dayOfWeek: Int,

    @field:NotNull
    val startTime: LocalTime,

    @field:NotNull
    val endTime: LocalTime
)
