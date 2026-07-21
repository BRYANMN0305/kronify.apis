/**
 * Repositorio que gestiona los planes de suscripción del sistema.
 * Permite buscar un plan por su nombre.
 */
package co.com.kronifyapis.repository

import co.com.kronifyapis.model.Plan
import org.springframework.data.jpa.repository.JpaRepository

interface PlanRepository : JpaRepository<Plan, Long> {

    fun findByName(name: String): Plan?
}
