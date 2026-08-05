package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Business
import co.com.kronifyapis.model.BusinessOpeningHour
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona las operaciones de base de datos para los horarios
 * de atención del negocio. Proporciona métodos para listar y buscar horarios
 * por negocio y día de la semana.
 */

interface BusinessOpeningHourRepository : JpaRepository<BusinessOpeningHour, Long> {

    fun findAllByBusiness(business: Business): List<BusinessOpeningHour>

    fun findAllByBusinessAndActiveTrue(business: Business): List<BusinessOpeningHour>

    fun findByBusinessAndDayOfWeekAndActiveTrue(business: Business, dayOfWeek: Int): BusinessOpeningHour?
}