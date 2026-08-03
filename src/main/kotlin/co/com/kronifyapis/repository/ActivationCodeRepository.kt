package co.com.kronifyapis.repository

import co.com.kronifyapis.model.ActivationCode
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ActivationCodeRepository : JpaRepository<ActivationCode, Long> {

    fun findByCodeAndActiveTrue(code: String): Optional<ActivationCode>

    fun findByPlan_PlanIdAndActiveTrue(planId: Long): List<ActivationCode>

    fun findByUsedAndActiveTrue(used: Boolean): List<ActivationCode>

    fun findAllByActiveTrue(): List<ActivationCode>
}
