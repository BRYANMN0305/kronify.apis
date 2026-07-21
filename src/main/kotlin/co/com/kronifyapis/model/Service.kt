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
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Modelo que representa un servicio ofrecido por un negocio.
 * Cada servicio pertenece a un solo negocio y tiene una duración y precio.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla y sus restricciones únicas.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @ManyToOne indica que varios servicios pueden pertenecer a un mismo negocio.
 * @JoinColumn indica la columna usada para la relación con la tabla negocio.
 * @UniqueConstraint evita que un negocio tenga dos servicios con el mismo nombre.
 * @PreUpdate para actualizar la fecha de modificación ante cualquier cambio.
 */

@Entity
@Table(
    name = "services",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["business_id", "name"])
    ]
)
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    var serviceId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business? = null,

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "duration_minutes", nullable = false)
    var durationMinutes: Int = 0,

    @Column(name = "price")
    var price: Double? = null,

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
