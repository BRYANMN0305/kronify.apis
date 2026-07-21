package co.com.kronifyapis.dto.employee

import java.time.LocalDateTime

/**
 * DTO que devuelve la información de un bloqueo en la agenda.
 */

data class ScheduleBlockResponse(
    val scheduleBlockId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val reason: String?
)
