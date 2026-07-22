package co.com.kronifyapis.repository


import co.com.kronifyapis.model.Customer
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona las operaciones de base de datos para los clientes.
 */

interface CustomerRepository : JpaRepository<Customer, Long> {

    //Busca por email y lo lista
    fun findByEmail(email: String): List<Customer>

    //Busca por numero de telefono y lo lista
    fun findByPhoneNumber(phoneNumber: String): List<Customer>

    //Busca por id del usuario
    fun findByUser_UserId(userId: Long): Customer?
}
