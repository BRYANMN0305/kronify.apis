package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Appointment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface AppointmentRepository : JpaRepository<Appointment, Long> {

    fun findAllByBusiness_BusinessId(businessId: Long): List<Appointment>

    fun findByAppointmentIdAndBusiness_BusinessId(appointmentId: Long, businessId: Long): Appointment?

    fun findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
        employeeId: Long,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): List<Appointment>

    fun findAllByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
        employeeId: Long,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): List<Appointment>

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.business.businessId = :businessId AND a.startAt >= :start AND a.startAt < :end AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    fun countByBusinessInDateRange(
        @Param("businessId") businessId: Long,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): Long
}
