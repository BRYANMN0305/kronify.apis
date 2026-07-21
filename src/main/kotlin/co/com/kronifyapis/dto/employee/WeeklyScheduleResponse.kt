package co.com.kronifyapis.dto.employee

import java.time.LocalTime

/**
 * DTO que devuelve la información de un horario semanal.
 */

data class WeeklyScheduleResponse(
    val weeklyScheduleId: Long,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)
