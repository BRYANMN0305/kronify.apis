package co.com.kronifyapis.model

import co.com.kronifyapis.model.enums.ProfileType
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

/**
 * Modelo que representa un usuario en la base de datos.
 * Un usuario puede ser de tipo CLIENTE o NEGOCIO, y es la base
 * para empleados, clientes registrados y dueños de negocios.
 *
 * Anotaciones utilizadas:
 *
 * @Entity indica que esta clase es una entidad JPA.
 * @Table especifica el nombre de la tabla en la base de datos.
 * @Id indica que esta columna es la clave primaria de la tabla.
 * @GeneratedValue indica que el valor de esta columna se genera automáticamente.
 * @Enumerated indica que esta columna guarda un valor de un enum.
 * @PreUpdate para actualizar la fecha de modificación ante cualquier cambio.
 */

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var userId: Long? = null,

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


    @Column(name = "password_hash")
    var passwordHash: String = "",

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
