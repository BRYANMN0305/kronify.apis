package co.com.kronifyapis.repository

import co.com.kronifyapis.model.ActivationCode
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ActivationCodeRepository : JpaRepository<ActivationCode, Long> {

    fun findByCode(code: String): Optional<ActivationCode>

    fun findByPlan_PlanId(planId: Long): List<ActivationCode>

    fun findByUsed(used: Boolean): List<ActivationCode>
}
