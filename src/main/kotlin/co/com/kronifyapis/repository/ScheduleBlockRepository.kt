package co.com.kronifyapis.repository

import co.com.kronifyapis.model.ScheduleBlock
import co.com.kronifyapis.model.Employee
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.UUID

interface ScheduleBlockRepository : JpaRepository<ScheduleBlock, UUID> {
    fun findByEmployeeAndStartAtLessThanAndEndAtGreaterThan(
        employee: Employee,
        dayEnd: LocalDateTime,
        dayStart: LocalDateTime
    ): List<ScheduleBlock>
}
