package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Employee
import co.com.kronifyapis.model.EmployeeService
import co.com.kronifyapis.model.Service
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeServiceRepository : JpaRepository<EmployeeService, UUID> {

    fun findAllByEmployee(employee: Employee): List<EmployeeService>

    fun findByEmployeeAndService(employee: Employee, service: Service): EmployeeService?

    fun existsByEmployeeAndService(employee: Employee, service: Service): Boolean

    fun deleteByEmployeeAndService(employee: Employee, service: Service)
}
