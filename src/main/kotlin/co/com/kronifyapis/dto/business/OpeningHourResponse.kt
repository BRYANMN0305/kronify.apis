package co.com.kronifyapis.dto.business

import java.time.LocalTime

/**
 * DTO que devuelve la información de un horario de atención del negocio.
 */

data class OpeningHourResponse(
    val businessOpeningHourId: Long,
    val dayOfWeek: Int,
    val startTime: LocalTime,
    val endTime: LocalTime
)