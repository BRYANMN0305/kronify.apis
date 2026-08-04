package co.com.kronifyapis.repository


import co.com.kronifyapis.model.Customer
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona las operaciones de base de datos para los clientes.
 */

interface CustomerRepository : JpaRepository<Customer, Long> {

    //Busca el cliente invitado por email dentro de un negocio
    fun findFirstByBusinessBusinessIdAndEmail(businessId: Long, email: String): Customer?

    //Busca el cliente invitado por numero de telefono dentro de un negocio
    fun findFirstByBusinessBusinessIdAndPhoneNumber(businessId: Long, phoneNumber: String): Customer?

    //Busca por id del usuario
    fun findByUser_UserId(userId: Long): Customer?
}