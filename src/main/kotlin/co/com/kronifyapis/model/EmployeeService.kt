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

/**
 * Modelo que relaciona un empleado con los servicios que puede realizar.
 * Es una tabla intermedia (muchos a muchos) entre empleados y servicios.
 * Un empleado puede tener varios servicios y un servicio puede ser realizado
 * por varios empleados.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla y sus restricciones únicas.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @ManyToOne indica que varias asignaciones pueden pertenecer a un mismo empleado o servicio.
 * @JoinColumn indica la columna usada para la relación.
 * @UniqueConstraint evita asignar el mismo servicio dos veces al mismo empleado.
 */

@Entity
@Table(
    name = "employee_services",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["service_id", "employee_id"])
    ]
)
data class EmployeeService(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_service_id")
    var employeeServiceId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    var service: Service? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    var employee: Employee? = null
)
