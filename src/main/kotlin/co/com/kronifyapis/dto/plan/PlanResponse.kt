
package co.com.kronifyapis.dto.plan

/**
 * DTO que devuelve la información de un plan de suscripción.
 * Incluye los límites de servicios, citas mensuales y empleados.
 */

data class PlanResponse(
    val planId: Long,
    val name: String,
    val serviceLimit: Int?,
    val monthlyAppointmentLimit: Int?,
    val employeeLimit: Int?
)
