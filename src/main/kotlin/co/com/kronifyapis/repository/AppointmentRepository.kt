package co.com.kronifyapis.repository


import co.com.kronifyapis.model.Appointment
import co.com.kronifyapis.model.enums.AppointmentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

/**
 * Repositorio que gestiona las operaciones de base de datos para las citas.
 * Proporciona métodos para buscar citas por negocio, empleado y horario,
 * además de contar citas en un rango de fechas (para límites del plan).
 */

interface AppointmentRepository : JpaRepository<Appointment, Long> {

    fun findAllByBusiness_BusinessId(businessId: Long, pageable: Pageable): Page<Appointment>

    fun findAllByCustomer_User_UserIdOrderByStartAtDesc(userId: Long, pageable: Pageable): Page<Appointment>

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

    //Busca citas futuras por servicio (para validar soft delete)
    fun findByServiceServiceIdAndStartAtGreaterThanEqualAndStatusIn(
        serviceId: Long,
        startAt: LocalDateTime,
        statuses: List<AppointmentStatus>
    ): List<Appointment>

    //Cuenta citas por negocio, rango de fechas y estado
    fun countByBusiness_BusinessIdAndStartAtGreaterThanEqualAndStartAtLessThanAndStatusNotIn(
        businessId: Long,
        start: LocalDateTime,
        end: LocalDateTime,
        statuses: List<AppointmentStatus>
    ): Long

    //Citas vencidas que siguen en un estado, con sus relaciones cargadas para notificar
    @EntityGraph(attributePaths = ["business", "service", "employee", "employee.user", "customer", "customer.user"])
    @Query(
        """
        select a
          from Appointment a
         where a.status = :status
           and a.startAt < :cutoff
        """
    )
    fun findExpiredAppointments(
        @Param("status") status: AppointmentStatus,
        @Param("cutoff") cutoff: LocalDateTime
    ): List<Appointment>

    //Citas confirmadas próximas a comenzar que todavía no recibieron recordatorio
    @EntityGraph(attributePaths = ["business", "service", "employee", "employee.user", "customer", "customer.user"])
    @Query(
        """
        select a
          from Appointment a
         where a.status = :status
           and a.reminderSentAt is null
           and a.startAt > :windowStart
           and a.startAt <= :windowEnd
        """
    )
    fun findAppointmentsPendingReminder(
        @Param("status") status: AppointmentStatus,
        @Param("windowStart") windowStart: LocalDateTime,
        @Param("windowEnd") windowEnd: LocalDateTime
    ): List<Appointment>

    //Cierra en bloque las citas que siguen en un estado y ya vencieron (cierre automático)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update Appointment a
           set a.status = :newStatus,
               a.cancellationReason = :reason,
               a.updatedAt = :now
         where a.status = :currentStatus
           and a.startAt < :cutoff
        """
    )
    fun closeExpiredAppointments(
        @Param("currentStatus") currentStatus: AppointmentStatus,
        @Param("newStatus") newStatus: AppointmentStatus,
        @Param("reason") reason: String,
        @Param("cutoff") cutoff: LocalDateTime,
        @Param("now") now: LocalDateTime
    ): Int
}
