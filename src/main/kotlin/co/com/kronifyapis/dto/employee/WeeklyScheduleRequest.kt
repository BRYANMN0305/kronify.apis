package co.com.kronifyapis.dto.employee

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

/**
 * DTO que recibe los datos para crear o actualizar un horario semanal.
 * dayOfWeek va de 1 = lunes a 7 = domingo.
 */

data class WeeklyScheduleRequest(
    @field:NotNull
    @field:Min(1, message = "El día debe estar entre 1 = lunes y 7 = domingo")
    @field:Max(7, message = "El día debe estar entre 1 = lunes y 7 = domingo")
    val dayOfWeek: Int,

    @field:NotNull
    val startTime: LocalTime,

    @field:NotNull
    val endTime: LocalTime
)
