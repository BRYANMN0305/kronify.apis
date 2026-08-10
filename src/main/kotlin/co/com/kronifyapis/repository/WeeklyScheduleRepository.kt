package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.WeeklySchedule
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona los horarios semanales de los empleados.
 * Permite consultar, eliminar y buscar horarios por empleado.
 */

interface WeeklyScheduleRepository : JpaRepository<WeeklySchedule, Long> {

    //Busca todos los horarios semanales de un empleado (activos o no)
    fun findAllByEmployee(employee: Employee): List<WeeklySchedule>

    //Busca todos los horarios semanales activos de un empleado
    fun findAllByEmployeeAndActiveTrue(employee: Employee): List<WeeklySchedule>

    //Busca un horario semanal activo por su ID y empleado
    fun findByWeeklyScheduleIdAndEmployeeAndActiveTrue(weeklyScheduleId: Long, employee: Employee): WeeklySchedule?

    //Busca un horario semanal activo por empleado y día de la semana
    fun findByEmployeeAndDayOfWeekAndActiveTrue(employee: Employee, dayOfWeek: Int): WeeklySchedule?
}
