package co.com.kronifyapis.dto.employee

import java.time.LocalTime
import java.util.UUID

data class WeeklyScheduleResponse(
    val weeklyScheduleId: UUID,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)
