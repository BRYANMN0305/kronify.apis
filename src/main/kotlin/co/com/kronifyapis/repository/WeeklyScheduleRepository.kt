package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.WeeklySchedule
import org.springframework.data.jpa.repository.JpaRepository

interface WeeklyScheduleRepository : JpaRepository<WeeklySchedule, Long> {

    fun findAllByEmployee(employee: Employee): List<WeeklySchedule>

    fun findByWeeklyScheduleIdAndEmployee(weeklyScheduleId: Long, employee: Employee): WeeklySchedule?

    fun deleteAllByEmployee(employee: Employee)

    fun findByEmployeeAndDayOfWeek(employee: Employee, dayOfWeek: Int): WeeklySchedule?
}