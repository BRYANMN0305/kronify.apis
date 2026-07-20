package co.com.kronifyapis.repository

import co.com.kronifyapis.model.WeeklySchedule
import co.com.kronifyapis.model.Employee
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WeeklyScheduleRepository : JpaRepository<WeeklySchedule, UUID> {
    fun findByEmployeeAndDayOfWeek(employee: Employee, dayOfWeek: Int): List<WeeklySchedule>
}
