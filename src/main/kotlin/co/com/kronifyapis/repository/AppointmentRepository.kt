package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Appointment
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.dto.appointment.AppointmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.UUID

interface AppointmentRepository : JpaRepository<Appointment, UUID> {
    fun findByEmployeeAndStartAtLessThanAndEndAtGreaterThanAndStatusNot(
        employee: Employee,
        dayEnd: LocalDateTime,
        dayStart: LocalDateTime,
        excludedStatus: AppointmentStatus = AppointmentStatus.CANCELLED
    ): List<Appointment>
}
