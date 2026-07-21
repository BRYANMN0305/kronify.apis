package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Service
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona los servicios ofrecidos por los negocios.
 * Permite buscar servicios por negocio, por nombre y verificar existencia.
 */

interface ServiceRepository : JpaRepository<Service, Long> {

    //Busca un servicio por su ID y negocio
    fun findByBusiness_BusinessIdAndName(businessId: Long, name: String): Service?

    //Busca todos los servicios por negocio
    fun findAllByBusinessBusinessId(businessId: Long): List<Service>

    //Busca un servicio por su ID y negocio
    fun findByServiceIdAndBusinessBusinessId(serviceId: Long, businessId: Long): Service?

    //Cuenta los servicios por negocio
    fun countByBusiness_BusinessId(businessId: Long): Long
}