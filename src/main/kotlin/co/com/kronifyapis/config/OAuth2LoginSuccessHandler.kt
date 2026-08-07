package co.com.kronifyapis.config

import co.com.kronifyapis.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

/**
 * Handler que se ejecuta cuando el proveedor OAuth2 (Google, Microsoft)
 * autenticó al usuario y Spring Security ya intercambió el code por un token.
 *
 * Convierte la identidad del proveedor en un usuario interno de Kronify y
 * redirige al frontend con el JWT propio de la aplicación.
 *
 * - Usuario nuevo  -> /oauth/callback?token=...&newProfile=true (elige tipo de perfil)
 * - Usuario existente -> /oauth/callback?token=...
 */
@Component
class OAuth2LoginSuccessHandler(
    private val authService: AuthService,
    @param:Value("\${app.frontend-url}") private val frontendUrl: String
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val oauthToken = authentication as OAuth2AuthenticationToken
            val registrationId = oauthToken.authorizedClientRegistrationId
            val oauth2User = oauthToken.principal

            val result = authService.loginWithOAuth(
                provider = registrationId,
                providerUserId = oauth2User.name,
                email = extractEmail(oauth2User),
                name = oauth2User.getAttribute<String>("name"),
                lastName = oauth2User.getAttribute<String>("family_name")
            )

            val callback = buildString {
                append(frontendUrl)
                append("/oauth/callback?token=")
                append(result.token.accessToken)
                if (result.newUser) append("&newProfile=true")
            }
            response.sendRedirect(callback)
        } catch (_: Exception) {
            // Errores de negocio (usuario desactivado, proveedor sin correo, etc.)
            response.sendRedirect("$frontendUrl/iniciar-sesion?error=oauth")
        }
    }

    /**
     * Microsoft envía el correo en "email", "preferred_username" o "upn"
     * según el tipo de cuenta; Google lo envía siempre en "email".
     */
    private fun extractEmail(user: OAuth2User): String? {
        return user.getAttribute<String>("email")
            ?: user.getAttribute<String>("preferred_username")
            ?: user.getAttribute<String>("upn")
    }
}