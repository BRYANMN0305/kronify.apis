package co.com.kronifyapis.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "plans")
data class Plan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    var planId: Long? = null,

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "service_limit")
    var serviceLimit: Int? = null,

    @Column(name = "monthly_appointment_limit")
    var monthlyAppointmentLimit: Int? = null,

    @Column(name = "employee_limit")
    var employeeLimit: Int? = null
)
