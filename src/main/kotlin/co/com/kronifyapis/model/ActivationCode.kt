package co.com.kronifyapis.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "activation_codes")
data class ActivationCode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activation_code_id")
    var activationCodeId: Long? = null,

    @Column(name = "code", nullable = false, unique = true)
    var code: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: Plan? = null,

    @Column(name = "used", nullable = false)
    var used: Boolean = false,

    @Column(name = "used_at")
    var usedAt: LocalDateTime? = null,

    @Column(name = "used_by_business_id")
    var usedByBusinessId: Long? = null,

    @Column(name = "expires_at")
    var expiresAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime? = null
)
