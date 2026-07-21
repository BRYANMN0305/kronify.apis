
package co.com.kronifyapis.repository

import co.com.kronifyapis.model.BusinessPlan
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Repositorio que gestiona la asignación de planes a negocios.
 */

interface BusinessPlanRepository : JpaRepository<BusinessPlan, Long> {

    // Busca el plan activo de un negocio
    fun findByBusiness_BusinessIdAndActiveTrue(businessId: Long): BusinessPlan?

    // Busca el plan activo con bloqueo pesimista para control de concurrencia en límites
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bp FROM BusinessPlan bp WHERE bp.business.businessId = :businessId AND bp.active = true")
    fun findActiveWithLock(@Param("businessId") businessId: Long): BusinessPlan?
}
