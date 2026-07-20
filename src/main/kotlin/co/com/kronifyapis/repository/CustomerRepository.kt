package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Customer
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository : JpaRepository<Customer, Long> {

    fun findByUser_UserId(userId: Long): Customer?
}
