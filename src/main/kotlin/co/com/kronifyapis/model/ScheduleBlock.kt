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
 * Modelo que representa un bloqueo en la agenda de un empleado.
 * Sirve para marcar horas NO disponibles de forma puntual (ej: día libre,
 * capacitación, emergencia). Se diferencia del horario semanal porque
 * es una excepción para una fecha específica.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla en la base de datos.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @ManyToOne indica que varios bloqueos pertenecen a un mismo empleado.
 * @JoinColumn indica la columna usada para la relación con la tabla empleado.
 */

@Entity
@Table(name = "schedule_blocks")
data class ScheduleBlock(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_block_id")
    var scheduleBlockId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null,

    @Column(name = "start_at", nullable = false)
    var startAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "end_at", nullable = false)
    var endAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "reason")
    var reason: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
