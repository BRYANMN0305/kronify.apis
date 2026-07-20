package co.com.kronifyapis.repository

import co.com.kronifyapis.model.OauthAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OauthAccountRepository : JpaRepository<OauthAccount, UUID> {

    fun findAllByUser_UserId(userId: UUID): List<OauthAccount>

    fun findByProviderAndProviderUserId(provider: String, providerUserId: String): OauthAccount?
}
