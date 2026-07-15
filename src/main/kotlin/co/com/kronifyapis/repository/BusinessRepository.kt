package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BusinessRepository: JpaRepository<Business, UUID> {

    fun findByOwner(owner: User): Business?

    fun findByBusinessId(businessId: UUID): Business?

    fun findBusinessBySlug (slug: String): Business?

    fun existsBusinessBySlug (slug: String): Boolean

    fun existsByOwner(user: User): Boolean

}
