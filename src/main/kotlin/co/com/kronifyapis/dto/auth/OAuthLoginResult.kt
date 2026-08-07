package co.com.kronifyapis.dto.auth

/**
 * Resultado del login mediante proveedor OAuth (Google, Microsoft).
 * `newUser` indica si el usuario fue creado en esta operación
 * (el frontend debe pedirle el tipo de perfil en ese caso).
 */
data class OAuthLoginResult(
    val token: TokenResponse,
    val newUser: Boolean
)