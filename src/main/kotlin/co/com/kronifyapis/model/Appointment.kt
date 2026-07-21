package co.com.kronifyapis.model

import co.com.kronifyapis.model.enums.AppointmentOrigin
import co.com.kronifyapis.model.enums.AppointmentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 *Representación de la base de datos que almacena las citas agendadas,
 * este modelo relaciona al negocio, servicio, empleado y cliente.
 *
 * Anotaciones utilizadas:
 *
 * @Entity para indicar que esta clase es una entidad JPA.
 * @Table para indicar el nombre de la tabla en la base de datos.
 * @Id para indicar que esta columna es la clave primaria de la tabla.
 * @GeneratedValue para indicar que el valor de la clave primaria se genera automáticamente.
 * @JoinColumn para indicar la columna de la tabla que se usa para la relación con otra tabla.
 * @PreUpdate para actualizar la fecha de modificación en la tabla ante cualquier cambio realizado.
 * @ManyToOne para indicar que esta columna es una relación de muchos a uno con otra tabla.
 */

@Entity
@Table(name = "appointments")
data class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    var appointmentId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: Service? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer? = null,

    @Column(name = "start_at", nullable = false)
    var startAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "end_at", nullable = false)
    var endAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: AppointmentStatus = AppointmentStatus.PENDING,

    @Column(name = "origin", nullable = false)
    @Enumerated(EnumType.STRING)
    var origin: AppointmentOrigin = AppointmentOrigin.PUBLIC,

    @Column(name = "cancellation_reason")
    var cancellationReason: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
