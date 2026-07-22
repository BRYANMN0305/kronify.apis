package co.com.kronifyapis.service

import java.time.LocalTime

data class BusyInterval(
    val start: LocalTime,
    val end: LocalTime
)

object AvailabilityCalculator {

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
            if (candidateEnd.isAfter(workingEnd)) break

            val overlaps = busyIntervals.any { busy ->
                candidateStart.isBefore(busy.end) && candidateEnd.isAfter(busy.start)
            }

            if (!overlaps) {
                slots.add(candidateStart)
            }

            val nextStart = candidateStart.plusMinutes(stepMinutes.toLong())
            if (!nextStart.isAfter(candidateStart)) break
            candidateStart = nextStart
        }

        return slots
    }
}
