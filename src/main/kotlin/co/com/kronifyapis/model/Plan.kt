package co.com.kronifyapis.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * Modelo que representa un plan de suscripción.
 * Define los límites que tiene un negocio según el plan contratado
 * (cantidad de servicios, citas mensuales, empleados).
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla en la base de datos.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 */

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
