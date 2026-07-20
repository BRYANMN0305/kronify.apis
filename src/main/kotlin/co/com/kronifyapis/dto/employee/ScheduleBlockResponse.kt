package co.com.kronifyapis.dto.employee

import java.time.LocalDateTime
import java.util.UUID

data class ScheduleBlockResponse(
    val scheduleBlockId: UUID,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val reason: String?
)
