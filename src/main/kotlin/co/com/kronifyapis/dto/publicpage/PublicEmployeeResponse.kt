package co.com.kronifyapis.dto.publicpage

data class PublicEmployeeResponse(
    val employeeId: Long,
    val name: String,
    val serviceIds: List<Long>
)