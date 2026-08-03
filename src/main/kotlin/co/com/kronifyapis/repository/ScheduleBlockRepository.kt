package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.ScheduleBlock
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

/**
 * Repositorio que gestiona los bloqueos de agenda de los empleados.
 * Permite consultar bloqueos por empleado y verificar si hay cruces de horarios.
 */

interface ScheduleBlockRepository : JpaRepository<ScheduleBlock, Long> {

    //Busca todos los bloqueos de agenda activos por empleado
    fun findAllByEmployeeAndActiveTrue(employee: Employee): List<ScheduleBlock>

    //Busca un bloqueo de agenda activo por su ID y empleado
    fun findByScheduleBlockIdAndEmployeeAndActiveTrue(scheduleBlockId: Long, employee: Employee): ScheduleBlock?

    //Verifica si existe un bloqueo de agenda activo para un empleado en un rango de tiempo
    fun existsByEmployeeAndStartAtLessThanAndEndAtGreaterThanAndActiveTrue(
        employee: Employee,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): Boolean

    fun findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThanAndActiveTrue(
        employee: Employee,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): List<ScheduleBlock>
}
