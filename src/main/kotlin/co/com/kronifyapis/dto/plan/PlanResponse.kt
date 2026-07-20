package co.com.kronifyapis.dto.plan

data class PlanResponse(
    val planId: Long,
    val name: String,
    val serviceLimit: Int?,
    val monthlyAppointmentLimit: Int?,
    val employeeLimit: Int?
)
