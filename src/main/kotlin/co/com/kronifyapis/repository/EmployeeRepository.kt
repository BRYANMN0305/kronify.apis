package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeRepository : JpaRepository<Employee, UUID> {
    fun existsByUser(user: User): Boolean

    fun existsByUserAndBusiness(user: User, business: Business): Boolean

    fun findAllByBusiness_BusinessId(businessId: UUID): List<Employee>

    fun findByEmployeeIdAndBusiness_BusinessId(employeeId: UUID, businessId: UUID): Employee?

    fun findByUserAndBusiness(user: User, business: Business): Employee?
}
