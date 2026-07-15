package co.com.kronifyapis.repository

import co.com.kronifyapis.dto.employeeInvitation.StatusType
import co.com.kronifyapis.model.EmployeeInvitation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EmployeeInvitationRepository : JpaRepository<EmployeeInvitation, UUID> {

    fun findByToken(token: String): EmployeeInvitation?

    fun findByBusiness_BusinessIdAndEmailAndStatus(
        businessId: UUID,
        email: String,
        status: StatusType
    ): EmployeeInvitation?

    fun findFirstByEmailAndStatus(email: String, status: StatusType): EmployeeInvitation?

    fun findAllByBusiness_BusinessId(businessId: UUID): List<EmployeeInvitation>
}
