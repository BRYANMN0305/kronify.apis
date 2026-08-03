package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Service
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona los servicios ofrecidos por los negocios.
 * Permite buscar servicios por negocio, por nombre y verificar existencia.
 */

interface ServiceRepository : JpaRepository<Service, Long> {

    //Busca un servicio por su ID y negocio
    fun findByBusiness_BusinessIdAndNameAndActiveTrue(businessId: Long, name: String): Service?

    //Busca todos los servicios activos por negocio
    fun findAllByBusinessBusinessIdAndActiveTrue(businessId: Long): List<Service>

    //Busca un servicio activo por su ID y negocio
    fun findByServiceIdAndBusinessBusinessIdAndActiveTrue(serviceId: Long, businessId: Long): Service?

    //Cuenta los servicios activos por negocio
    fun countByBusiness_BusinessIdAndActiveTrue(businessId: Long): Long
}
