package co.com.kronifyapis.model

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
import co.com.kronifyapis.model.enums.StatusType
import java.time.LocalDateTime

/**
 * Modelo que representa una invitación de empleado en la base de datos.
 * Esta entidad se relaciona con un negocio.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla en la base de datos.
 * @Id indica que el campo invitationId es la clave primaria de la entidad.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @ManyToOne indica que esta entidad tiene una relación muchos a uno con otra tabla.
 * @JoinColumn indica la columna de la tabla que se usa para la relación con otra tabla.
 * @PreUpdate para actualizar la fecha de modificación en la tabla ante cualquier cambio realizado.
 *
 */

@Entity
@Table(name = "employee_invitations")
data class EmployeeInvitation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    var invitationId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business? = null,

    @Column(name = "email", nullable = false)
    var email: String = "",

    @Column(name = "token", nullable = false, unique = true)
    var token: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: StatusType = StatusType.PENDING,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by_user_id")
    var acceptedBy: User? = null,

    @Column(name = "accepted_at")
    var acceptedAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
