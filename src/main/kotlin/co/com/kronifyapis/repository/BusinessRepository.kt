package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona las operaciones de base de datos para los negocios.
 * Proporciona métodos para buscar negocios por dueño, slug y verificar existencia.
 */

interface BusinessRepository: JpaRepository<Business, Long> {

    fun findByBusinessId(businessId: Long): Business?

    //Busca un negocio por su dueño
    fun findByOwner(owner: User): Business?

    fun findBusinessBySlug(slug: String): Business?

    //Verifica que exista un negocio por su slug
    fun existsBusinessBySlug (slug: String): Boolean

    //Verifica que exista un negocio por su dueño
    fun existsByOwner(user: User): Boolean

}
