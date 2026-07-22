package co.com.kronifyapis.repository


import co.com.kronifyapis.model.Appointment
import co.com.kronifyapis.model.enums.AppointmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

/**
 * Repositorio que gestiona las operaciones de base de datos para las citas.
 * Proporciona métodos para buscar citas por negocio, empleado y horario,
 * además de contar citas en un rango de fechas (para límites del plan).
 */

interface AppointmentRepository : JpaRepository<Appointment, Long> {

    //Busca todas las citas por negocio y las lista
    fun findAllByBusiness_BusinessId(businessId: Long): List<Appointment>

    fun findAllByBusiness_BusinessIdAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
        businessId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Appointment>

    fun findAllByBusiness_BusinessIdAndEmployee_EmployeeIdAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
        businessId: Long,
        employeeId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Appointment>

    //Busca una cita por su ID y el ID del negocio al que pertenece
    fun findByAppointmentIdAndBusiness_BusinessId(appointmentId: Long, businessId: Long): Appointment?

    //Busca citas por empleado y horario
    fun findByEmployee_EmployeeIdAndStartAtLessThanAndEndAtGreaterThan(
        employeeId: Long,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): List<Appointment>

    //Cuenta citas por negocio, rango de fechas y estado
    fun countByBusiness_BusinessIdAndStartAtGreaterThanEqualAndStartAtLessThanAndStatusNotIn(
        businessId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
        statuses: List<AppointmentStatus>
    ): Long
}
