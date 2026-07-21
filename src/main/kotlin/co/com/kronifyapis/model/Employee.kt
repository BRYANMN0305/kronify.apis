package co.com.kronifyapis.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

/**
 *Modelo que representa un empleado en la base de datos.
 * Esta entidad relaciona a un empleado con un usuario y un negocio.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla en la base de datos.
 * UniqueConstraint indica que user_id y business_id debe ser único para cada empleado.
 * @ManyToOne indica que esta entidad tiene una relación muchos a uno con otra tabla.
 * @JoinColumn indica la columna de la tabla que se usa para la relación con otra tabla.
 * @PreUpdate para actualizar la fecha de modificación en la tabla ante cualquier cambio realizado.
 *
 */

@Entity
@Table(
    name = "employees",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "business_id"])
    ]
)
data class Employee(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    var employeeId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business? = null,

    @Column(name = "owner", nullable = false)
    var owner: Boolean = false,

    @Column(name = "self_managed_schedule", nullable = false)
    var selfManagedSchedule: Boolean = true,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

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
