package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.WeeklySchedule
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona los horarios semanales de los empleados.
 * Permite consultar, eliminar y buscar horarios por empleado.
 */

interface WeeklyScheduleRepository : JpaRepository<WeeklySchedule, Long> {

    //Busca todos los horarios semanales de un empleado
    fun findAllByEmployee(employee: Employee): List<WeeklySchedule>

    //Busca un horario semanal por su ID y empleado
    fun findByWeeklyScheduleIdAndEmployee(weeklyScheduleId: Long, employee: Employee): WeeklySchedule?

    fun deleteAllByEmployee(employee: Employee)

    //Busca un horario semanal por empleado y día de la semana
    fun findByEmployeeAndDayOfWeek(employee: Employee, dayOfWeek: Int): WeeklySchedule?
}
