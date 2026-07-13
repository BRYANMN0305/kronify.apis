package co.com.kronifyapis.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "plan")
class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_id")
    var planId: UUID? = null

    @Column(name = "name", nullable = false)
    var name: String = ""

    @Column(name = "service_limit")
    var serviceLimit: Int? = null

    @Column(name = "monthly_appointment_limit")
    var monthlyAppointmentLimit: Int? = null
}

