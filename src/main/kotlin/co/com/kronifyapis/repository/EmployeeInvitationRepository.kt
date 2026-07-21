package co.com.kronifyapis.repository

import co.com.kronifyapis.model.enums.StatusType
import co.com.kronifyapis.model.EmployeeInvitation
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona las operaciones para las invitaciones de empleados.
 */

interface EmployeeInvitationRepository : JpaRepository<EmployeeInvitation, Long> {


    //Busca una invitación por su negocio, email y estado
    fun findByBusiness_BusinessIdAndEmailAndStatus(
        businessId: Long,
        email: String,
        status: StatusType
    ): EmployeeInvitation?

    //Busca una invitación por su email y estado
    fun findFirstByEmailAndStatus(email: String, status: StatusType): EmployeeInvitation?

    //Busca todas las invitaciones por su negocio
    fun findAllByBusiness_BusinessId(businessId: Long): List<EmployeeInvitation>

    //Busca una invitación por su token
    fun findByToken(token: String): EmployeeInvitation?

    //Busca una invitación por su token y estado
    fun findByTokenAndStatus(token: String, status: StatusType): EmployeeInvitation?
}
