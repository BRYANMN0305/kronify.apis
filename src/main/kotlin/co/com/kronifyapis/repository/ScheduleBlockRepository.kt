package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.ScheduleBlock
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ScheduleBlockRepository : JpaRepository<ScheduleBlock, Long> {

    fun findAllByEmployee(employee: Employee): List<ScheduleBlock>

    fun findByScheduleBlockIdAndEmployee(scheduleBlockId: Long, employee: Employee): ScheduleBlock?

    fun existsByEmployeeAndStartAtLessThanAndEndAtGreaterThan(
        employee: Employee,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): Boolean

    fun findAllByEmployeeAndStartAtLessThanAndEndAtGreaterThan(
        employee: Employee,
        endAt: LocalDateTime,
        startAt: LocalDateTime
    ): List<ScheduleBlock>
}