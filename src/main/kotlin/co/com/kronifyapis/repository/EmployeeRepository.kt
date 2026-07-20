package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun existsByUser(user: User): Boolean

    fun existsByUserAndBusiness(user: User, business: Business): Boolean

    fun findAllByBusiness_BusinessId(businessId: Long): List<Employee>

    fun findByEmployeeIdAndBusiness_BusinessId(employeeId: Long, businessId: Long): Employee?

    fun findByUserAndBusiness(user: User, business: Business): Employee?

    fun findAllByUser_UserId(userId: Long): List<Employee>

    fun countByBusiness_BusinessId(businessId: Long): Long
}
