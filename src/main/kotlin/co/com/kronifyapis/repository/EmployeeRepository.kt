package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeRepository : JpaRepository<Employee, UUID> {
    fun existsByUser(user: User): Boolean
}
