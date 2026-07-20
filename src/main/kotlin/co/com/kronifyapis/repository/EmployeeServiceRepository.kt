package co.com.kronifyapis.repository

import co.com.kronifyapis.model.EmployeeService
import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.Service
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeServiceRepository : JpaRepository<EmployeeService, UUID> {
    fun existsByEmployeeAndService(employee: Employee, service: Service): Boolean
}
