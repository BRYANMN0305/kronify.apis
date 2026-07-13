package co.com.kronifyapis.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "weekly_schedules")
class WeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "weekly_schedule_id")
    var weeklyScheduleId: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null

    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: Int = 0

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.MIN

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime = LocalTime.MIN
}

