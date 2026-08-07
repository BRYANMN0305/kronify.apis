package co.com.kronifyapis.repository

import co.com.kronifyapis.model.OauthAccount
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona las cuentas OAuth vinculadas a usuarios.
 * Permite buscar por usuario y por proveedor externo (Google, Microsoft).
 */

interface OauthAccountRepository : JpaRepository<OauthAccount, Long> {

    //Busca todas las cuentas OAuth por usuario
    fun findAllByUser_UserId(userId: Long): List<OauthAccount>

    //Verifica si el proveedor ya vinculó ese id externo de usuario
    fun existsByProviderAndProviderUserId(provider: String, providerUserId: String): Boolean

    //Verifica si el usuario ya tiene ese proveedor vinculado
    fun existsByProviderAndUser_UserId(provider: String, userId: Long): Boolean
}
