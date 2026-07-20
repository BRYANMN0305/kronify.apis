package co.com.kronifyapis.repository

import co.com.kronifyapis.dto.employeeInvitation.StatusType
import co.com.kronifyapis.model.EmployeeInvitation
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeInvitationRepository : JpaRepository<EmployeeInvitation, Long> {

    fun findByToken(token: String): EmployeeInvitation?

    fun findByBusiness_BusinessIdAndEmailAndStatus(
        businessId: Long,
        email: String,
        status: StatusType
    ): EmployeeInvitation?

    fun findFirstByEmailAndStatus(email: String, status: StatusType): EmployeeInvitation?

    fun findAllByBusiness_BusinessId(businessId: Long): List<EmployeeInvitation>
}
