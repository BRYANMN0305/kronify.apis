package co.com.kronifyapis.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "business")
class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "business_id")
    var businessId: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    var owner: User? = null

    @Column(name = "slug", nullable = false, unique = true)
    var slug: String = ""

    @Column(name = "name", nullable = false)
    var name: String = ""

    @Column(name = "category")
    var category: String? = null

    @Column(name = "description")
    var description: String? = null

    @Column(name = "logo_url")
    var logoUrl: String? = null

    @Column(name = "email")
    var email: String? = null

    @Column(name = "phone_number")
    var phoneNumber: String? = null

    @Column(name = "whatsapp")
    var whatsapp: String? = null

    @Column(name = "active", nullable = false)
    var active: Boolean = true

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

