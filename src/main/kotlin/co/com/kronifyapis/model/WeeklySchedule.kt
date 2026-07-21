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
 * Modelo que representa el horario semanal de un empleado.
 * Define qué días y en qué horario trabaja un empleado de forma regular.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla y sus restricciones únicas.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @ManyToOne indica que varios horarios pertenecen a un mismo empleado.
 * @JoinColumn indica la columna usada para la relación con la tabla empleado.
 * @UniqueConstraint evita que un empleado tenga dos horarios para el mismo día.
 */

@Entity
@Table(
    name = "weekly_schedules",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["employee_id", "day_of_week"])
    ]
)

//prueba
data class WeeklySchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_schedule_id")
    var weeklyScheduleId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null,

    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: Int = 0,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime = LocalTime.MIN,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime = LocalTime.MIN
)
