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

/**
 * Modelo que representa el plan adquirido por un negocio.
 * Este modelo relaciona un negocio con un plan.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla en la base de datos.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @ManyToOne indica que esta columna es una relación de muchos a uno con otra tabla.
 * @JoinColumn indica la columna de la tabla que se usa para la relación con otra tabla.
 */

@Entity
@Table(name = "business_plan")
data class BusinessPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_plan_id")
    var businessPlanId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: Plan? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @Column(name = "start_at")
    var startAt: LocalDateTime? = null,

    @Column(name = "end_at")
    var endAt: LocalDateTime? = null
)
