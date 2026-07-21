package co.com.kronifyapis.domain.availability

import java.time.LocalTime

data class BusyInterval(
    val start: LocalTime,
    val end: LocalTime
)

object AvailabilityCalculator {

    /**
     * Calcula los horarios de inicio disponibles dentro de un rango de trabajo,
     * evitando cualquier solapamiento con los intervalos ocupados (bloqueos + citas).
     *
     * @param workingStart hora de inicio de la jornada (ej. 09:00)
     * @param workingEnd hora de fin de la jornada (ej. 17:00)
     * @param durationMinutes duración del servicio a agendar
     * @param busyIntervals lista de intervalos ya ocupados (bloqueos y citas), ya normalizados a ese día
     * @param stepMinutes intervalo entre inicios de slot candidatos (por defecto, igual a la duración del servicio)
     */
    fun calculateAvailableSlots(
        workingStart: LocalTime,
        workingEnd: LocalTime,
        durationMinutes: Int,
        busyIntervals: List<BusyInterval>,
        stepMinutes: Int = durationMinutes
    ): List<LocalTime> {
        if (durationMinutes <= 0 || stepMinutes <= 0) return emptyList()
        if (!workingStart.isBefore(workingEnd)) return emptyList()

        val slots = mutableListOf<LocalTime>()
        var candidateStart = workingStart

        while (true) {
            val candidateEnd = candidateStart.plusMinutes(durationMinutes.toLong())

            // Si el slot candidato se pasa del fin de la jornada, terminamos
            if (candidateEnd.isAfter(workingEnd)) break

            val overlaps = busyIntervals.any { busy ->
                candidateStart.isBefore(busy.end) && candidateEnd.isAfter(busy.start)
            }

            if (!overlaps) {
                slots.add(candidateStart)
            }

            val nextStart = candidateStart.plusMinutes(stepMinutes.toLong())

            // Evita loop infinito si plusMinutes da la vuelta la medianoche (ej. workingEnd = 23:50)
            if (!nextStart.isAfter(candidateStart)) break
            candidateStart = nextStart
        }

        return slots
    }
}
