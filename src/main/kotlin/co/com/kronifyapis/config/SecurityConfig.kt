package co.com.kronifyapis.config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Configuración de seguridad de la aplicación.
 * Define qué rutas son públicas, cuáles requieren autenticación,
 * y agrega el filtro JWT antes de cualquier otro filtro de seguridad.
 */

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val adminApiKeyFilter: AdminApiKeyFilter,
    private val oauth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val oauth2LoginFailureHandler: OAuth2LoginFailureHandler
) {
    /**
     * Bean para codificar contraseñas con BCrypt.
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Configura la cadena de filtros de seguridad.
     *
     * Nota: sessionCreationPolicy es IF_REQUIRED (no STATELESS) porque el flujo
     * OAuth2 de Spring Security necesita almacenar el estado del "state" entre
     * la redirección al proveedor y el callback. La autenticación del API sigue
     * siendo 100% stateless vía JWT: para las rutas autenticadas nunca se crea
     * sesión, solo se usa durante el round-trip del login OAuth en el navegador.
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                it.requestMatchers(HttpMethod.GET, "/").permitAll()
                it.requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                it.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                it.requestMatchers(HttpMethod.POST, "/appointments/").permitAll()
                it.requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                it.requestMatchers("/uploads/**").permitAll()
                it.requestMatchers("/admin/**").permitAll()
                it.requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // Endpoints del flujo OAuth2 (Spring Security los expone automáticamente)
                it.requestMatchers("/oauth2/authorization/**").permitAll()
                it.requestMatchers("/login/oauth2/code/**").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2Login {
                it.successHandler(oauth2LoginSuccessHandler)
                it.failureHandler(oauth2LoginFailureHandler)
            }
            .addFilterBefore(adminApiKeyFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .build()
    }
}