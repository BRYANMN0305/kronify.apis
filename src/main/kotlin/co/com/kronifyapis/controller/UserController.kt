package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.dto.auth.LinkedAuthMethodResponse
import co.com.kronifyapis.dto.user.UserChangePasswordRequest
import co.com.kronifyapis.dto.user.UserChangePasswordResponse
import co.com.kronifyapis.dto.user.UserProfileResponse
import co.com.kronifyapis.dto.user.UserUpdateRequest
import co.com.kronifyapis.dto.user.UserUpdateResponse
import co.com.kronifyapis.service.AuthService
import co.com.kronifyapis.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador para que el usuario autenticado gestione su propia cuenta.
 */

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    private val authService: AuthService
) {

    /**
     * Devuelve el perfil del usuario autenticado.
     */
    @GetMapping("/profile")
    fun getProfile(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<UserProfileResponse> {
        return ResponseEntity.ok(userService.getProfile(user.userId))
    }

    /**
     * Actualiza los datos del perfil del usuario autenticado.
     */
    @PatchMapping("/update")
    fun updateUser(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserUpdateResponse> {
        val updatedUser = userService.updateUser(user.userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     */
    @PatchMapping("/updatePassword")
    fun updatePassword(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: UserChangePasswordRequest
    ): ResponseEntity<UserChangePasswordResponse> {
        val changedPassword = userService.changePassword(user.userId, request)
        return ResponseEntity.ok(changedPassword)
    }

    /**
     * Lista los métodos de autenticación vinculados a la cuenta
     */
    @GetMapping("/auth-methods")
    fun linkedAuthMethods(
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<List<LinkedAuthMethodResponse>> {
        return ResponseEntity.ok(authService.listLinkedAuthMethods(user.userId))
    }

}
