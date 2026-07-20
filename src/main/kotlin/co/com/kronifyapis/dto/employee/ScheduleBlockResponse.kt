package co.com.kronifyapis.dto.employee

import java.time.LocalDateTime

data class ScheduleBlockResponse(
    val scheduleBlockId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val reason: String?
)
