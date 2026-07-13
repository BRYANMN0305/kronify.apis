package co.com.kronifyapis.model

import co.com.kronifyapis.dto.ProfileType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "last_name", nullable = false)
    var lastName: String = "",

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @Column(name = "email", nullable = false, unique = true)
    var email: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false)
    var profileType: ProfileType = ProfileType.CLIENT,

    @Column(name = "verified_email", nullable = false)
    var verifiedEmail: Boolean = false,

    @Column(name = "password_hash")
    var passwordHash: String = "",

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return userId != null && userId == other.userId
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
