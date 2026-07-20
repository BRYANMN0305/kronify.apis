package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Service
import org.springframework.data.jpa.repository.JpaRepository

interface ServiceRepository : JpaRepository<Service, Long> {

    fun findByBusiness_BusinessIdAndName(businessId: Long, name: String): Service?

    fun findAllByBusinessBusinessId(businessId: Long): List<Service>

    fun findByServiceIdAndBusinessBusinessId(serviceId: Long, businessId: Long): Service?

    fun countByBusiness_BusinessId(businessId: Long): Long
}