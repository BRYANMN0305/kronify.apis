package co.com.kronifyapis.repository


import co.com.kronifyapis.model.Customer
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona las operaciones de base de datos para los clientes.
 */

interface CustomerRepository : JpaRepository<Customer, Long> {
    fun findByEmail(email: String): List<Customer>
    fun findByPhoneNumber(phoneNumber: String): List<Customer>
    fun findByUser_UserId(userId: Long): Customer?
}
