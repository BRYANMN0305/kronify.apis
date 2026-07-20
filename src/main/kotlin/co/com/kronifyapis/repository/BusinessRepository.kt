package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface BusinessRepository: JpaRepository<Business, Long> {

    fun findByOwner(owner: User): Business?

    fun findByBusinessId(businessId: Long): Business?

    fun findBusinessBySlug (slug: String): Business?

    fun existsBusinessBySlug (slug: String): Boolean

    fun existsByOwner(user: User): Boolean

}
