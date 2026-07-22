package co.com.kronifyapis.dto.publicpage

/**
 * DTO que devuelve la información de un empleado en la página pública del negocio,
 * junto con la lista de IDs de los servicios que puede realizar.
 */

data class PublicEmployeeResponse(
    val employeeId: Long,
    val name: String,
    val serviceIds: List<Long>
)