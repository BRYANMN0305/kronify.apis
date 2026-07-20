package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Service
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ServiceRepository : JpaRepository<Service, UUID> {

    fun findByBusiness_BusinessIdAndName(businessId: UUID, name: String): Service?

    fun findAllByBusinessBusinessId(businessId: UUID): List<Service>

    fun findByServiceIdAndBusinessBusinessId(serviceId: UUID, businessId: UUID): Service?
}
