
package co.com.kronifyapis.dto.auth

/**
 * DTO que devuelve el token JWT después de iniciar sesión.
 */

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)
