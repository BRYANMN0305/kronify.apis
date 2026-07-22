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
    fun findAllByEmployee(employee: Employee): List<EmployeeService>

    //Busca por servicio y lo lista
    fun findAllByService(service: Service): List<EmployeeService>

    //Busca por empleado y servicio y lo lista
    fun findByEmployeeAndService(employee: Employee, service: Service): EmployeeService?

    //Verifica si existe un registro con el empleado y servicio especificados
    fun existsByEmployeeAndService(employee: Employee, service: Service): Boolean

    //Elimina por empleado y servicio
    fun deleteByEmployeeAndService(employee: Employee, service: Service)

    //Elimina todos los registros de un servicio
    fun deleteAllByService(service: Service)
}
