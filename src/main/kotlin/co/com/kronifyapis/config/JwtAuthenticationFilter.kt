package co.com.kronifyapis.config

import co.com.kronifyapis.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filtro de seguridad que se ejecuta antes de cada petición para validar
 * el token JWT enviado en el encabezado Authorization.
 * Si el token es válido, establece la autenticación.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    /**
     * Extrae el token del header "Authorization: Bearer <token>",
     * lo valida y si es correcto, coloca al usuario autenticado en el contexto de seguridad.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Revisa que el header Authorization exista y tenga el formato Bearer
        val header = request.getHeader("Authorization")
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        // Limpia el token quitando el prefijo "Bearer "
        val token = header.removePrefix("Bearer ").trim()
        val authenticatedUser = jwtService.extractAuthenticatedUser(token)

        // Si el token es válido y aún no hay autenticación, la realiza
        if (authenticatedUser != null && jwtService.isTokenValid(token) && SecurityContextHolder.getContext().authentication == null) {
                // Crea una autoridad con el tipo de perfil del usuario.
                val authorities = listOf(SimpleGrantedAuthority("PROFILE_TYPE_${authenticatedUser.profileType.name}"))
                val authentication = UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}
