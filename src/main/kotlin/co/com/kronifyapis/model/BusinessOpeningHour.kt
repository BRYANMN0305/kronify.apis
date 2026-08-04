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

/**
 * Modelo que representa el horario semanal de atención de un negocio.
 * Define en qué días y en qué horario está abierto el negocio de forma regular.
 * Es el límite máximo para la disponibilidad: la agenda de cada empleado
 * está contenida dentro de este horario.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla y sus restricciones únicas.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @ManyToOne indica que varios horarios pertenecen a un mismo negocio.
 * @JoinColumn indica la columna usada para la relación con la tabla negocio.
 * @UniqueConstraint evita que un negocio tenga dos horarios para el mismo día.
 */

@Entity
@Table(
    name = "business_opening_hours",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["business_id", "day_of_week", "active"])
    ]
)
data class BusinessOpeningHour(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_opening_hour_id")
    var businessOpeningHourId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    var business: Business? = null,

    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: Int = 0,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.MIN,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime = LocalTime.MIN,

    @Column(name = "active", nullable = false, columnDefinition = "boolean not null default true")
    var active: Boolean = true
)