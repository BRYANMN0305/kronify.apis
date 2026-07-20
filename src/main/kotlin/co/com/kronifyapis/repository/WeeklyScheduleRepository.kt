package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.WeeklySchedule
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WeeklyScheduleRepository : JpaRepository<WeeklySchedule, UUID> {

    fun findAllByEmployee(employee: Employee): List<WeeklySchedule>

    fun findByWeeklyScheduleIdAndEmployee(weeklyScheduleId: UUID, employee: Employee): WeeklySchedule?

    fun deleteAllByEmployee(employee: Employee)
}
