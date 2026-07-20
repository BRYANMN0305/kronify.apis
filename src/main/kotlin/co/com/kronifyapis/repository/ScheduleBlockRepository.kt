package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.ScheduleBlock
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.UUID

interface ScheduleBlockRepository : JpaRepository<ScheduleBlock, UUID> {

    fun findAllByEmployee(employee: Employee): List<ScheduleBlock>

    fun findByScheduleBlockIdAndEmployee(scheduleBlockId: UUID, employee: Employee): ScheduleBlock?

    fun existsByEmployeeAndStartAtLessThanAndEndAtGreaterThan(
        employee: Employee,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): Boolean
}
