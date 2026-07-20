package co.com.kronifyapis.repository

import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByUserId(userId: Long): User?

    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean





}
