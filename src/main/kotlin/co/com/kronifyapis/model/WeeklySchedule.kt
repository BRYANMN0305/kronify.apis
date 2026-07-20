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
import jakarta.persistence.UniqueConstraint
import java.time.LocalTime

@Entity
@Table(
    name = "weekly_schedules",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["employee_id", "day_of_week"])
    ]
)
data class WeeklySchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_schedule_id")
    var weeklyScheduleId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null,

    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: Int = 0,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.MIN,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime = LocalTime.MIN
)
