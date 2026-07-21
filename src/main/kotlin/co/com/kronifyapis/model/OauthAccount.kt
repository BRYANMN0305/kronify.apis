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
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

/**
 * Modelo que vincula una cuenta de usuario con un proveedor externo
 * (Google, Microsoft, etc.). Permite que un usuario inicie sesión
 * usando su cuenta de redes sociales o servicios externos.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla y sus restricciones únicas.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @ManyToOne indica que varias cuentas OAuth pueden pertenecer a un mismo usuario.
 * @JoinColumn indica la columna usada para la relación.
 * @UniqueConstraint evita vincular el mismo proveedor dos veces al mismo usuario,
 * y evita que un mismo ID de proveedor se vincule a varios usuarios.
 */

@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "provider_user_id"]),
        UniqueConstraint(columnNames = ["user_id", "provider"])
    ]
)
data class OauthAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_id")
    var oauthId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Column(name = "provider", nullable = false)
    var provider: String = "",

    @Column(name = "provider_user_id", nullable = false)
    var providerUserId: String = "",

    @Column(name = "provider_email")
    var providerEmail: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
