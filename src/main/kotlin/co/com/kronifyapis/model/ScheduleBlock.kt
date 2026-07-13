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
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "schedule_blocks")
class ScheduleBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_block_id")
    var scheduleBlockId: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null

    @Column(name = "start_at", nullable = false)
    var startAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "end_at", nullable = false)
    var endAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "reason")
    var reason: String? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}

