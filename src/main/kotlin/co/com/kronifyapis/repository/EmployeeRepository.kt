package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona las operaciones para los empleados.
 */

interface EmployeeRepository : JpaRepository<Employee, Long> {

    //Verifica si existe un empleado por usuario y negocio
    fun existsByUserAndBusiness(user: User, business: Business): Boolean

    //Lista todos los empleados por negocio
    fun findAllByBusiness_BusinessId(businessId: Long): List<Employee>

    //Lista empleados activos por negocio
    fun findAllByBusiness_BusinessIdAndActiveTrue(businessId: Long): List<Employee>

    //Busca un empleado por su id y negocio
    fun findByEmployeeIdAndBusiness_BusinessId(employeeId: Long, businessId: Long): Employee?

    //Busca un empleado por su usuario y negocio
    fun findByUserAndBusiness(user: User, business: Business): Employee?

    //Lista todos los empleados por usuario
    fun findAllByUser_UserId(userId: Long): List<Employee>

    //Cuenta los empleados por negocio
    fun countByBusiness_BusinessId(businessId: Long): Long
}
