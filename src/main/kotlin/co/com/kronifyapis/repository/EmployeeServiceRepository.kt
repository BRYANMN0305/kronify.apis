package co.com.kronifyapis.repository

/**
 * Repositorio que gestiona la relación entre empleados y servicios.
 * Permite consultar qué servicios tiene asignado un empleado y viceversa.
 */
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.EmployeeService
import co.com.kronifyapis.model.Service
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeServiceRepository : JpaRepository<EmployeeService, Long> {

    //Busca por empleado y lo lista
    fun findAllByEmployeeAndActiveTrue(employee: Employee): List<EmployeeService>

    //Busca por servicio y lo lista
    fun findAllByServiceAndActiveTrue(service: Service): List<EmployeeService>

    //Busca por empleado y servicio (activo o no, para reusar la fila)
    fun findByEmployeeAndService(employee: Employee, service: Service): EmployeeService?

    //Busca por empleado y servicio y lo lista
    fun findByEmployeeAndServiceAndActiveTrue(employee: Employee, service: Service): EmployeeService?

    //Verifica si existe un registro activo con el empleado y servicio especificados
    fun existsByEmployeeAndServiceAndActiveTrue(employee: Employee, service: Service): Boolean
}
