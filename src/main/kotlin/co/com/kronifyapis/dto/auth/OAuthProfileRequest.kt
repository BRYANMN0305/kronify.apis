package co.com.kronifyapis.dto.auth

import co.com.kronifyapis.model.enums.ProfileType
import jakarta.validation.constraints.NotNull

/**
 * DTO que recibe el tipo de perfil elegido por un usuario
 * que inicia sesión por OAuth por primera vez.
 */
data class OAuthProfileRequest(
    @field:NotNull(message = "El tipo de perfil es obligatorio")
    val profileType: ProfileType
)