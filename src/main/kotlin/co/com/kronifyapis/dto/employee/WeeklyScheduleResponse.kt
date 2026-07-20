package co.com.kronifyapis.dto.employee

import java.time.LocalTime

data class WeeklyScheduleResponse(
    val weeklyScheduleId: Long,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)
