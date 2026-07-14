package co.com.kronifyapis.repository

import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {

    fun findByUserId(userId: UUID): User?

    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean





}
