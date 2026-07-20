package co.com.kronifyapis.repository

import co.com.kronifyapis.model.BusinessPlan
import org.springframework.data.jpa.repository.JpaRepository

interface BusinessPlanRepository : JpaRepository<BusinessPlan, Long> {

    fun findByBusiness_BusinessIdAndActiveTrue(businessId: Long): BusinessPlan?
}
