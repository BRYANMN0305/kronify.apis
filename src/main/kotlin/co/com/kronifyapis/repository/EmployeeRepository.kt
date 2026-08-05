package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.User
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Repositorio que gestiona las operaciones para los empleados.
 */

interface EmployeeRepository : JpaRepository<Employee, Long> {

    //Bloquea la fila del empleado para serializar la creación/reprogramación de citas
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Employee e where e.employeeId = :employeeId")
    fun findByIdForUpdate(@Param("employeeId") employeeId: Long): Employee?

    //Verifica si existe un empleado activo por usuario y negocio
    fun existsByUserAndBusinessAndActiveTrue(user: User, business: Business): Boolean

    //Lista todos los empleados activos por negocio
    fun findAllByBusiness_BusinessIdAndActiveTrue(businessId: Long): List<Employee>

    //Busca un empleado activo por su id y negocio
    fun findByEmployeeIdAndBusiness_BusinessIdAndActiveTrue(employeeId: Long, businessId: Long): Employee?

    //Busca un empleado activo por su usuario y negocio
    fun findByUserAndBusinessAndActiveTrue(user: User, business: Business): Employee?

    //Lista todos los empleados activos por usuario
    fun findAllByUser_UserIdAndActiveTrue(userId: Long): List<Employee>

    //Cuenta los empleados activos por negocio
    fun countByBusiness_BusinessIdAndActiveTrue(businessId: Long): Long
}
