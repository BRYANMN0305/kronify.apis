package co.com.kronifyapis.controller

import co.com.kronifyapis.config.auth.AuthenticatedUser
import co.com.kronifyapis.dto.user.UserChangePasswordRequest
import co.com.kronifyapis.dto.user.UserChangePasswordResponse
import co.com.kronifyapis.dto.user.UserUpdateRequest
import co.com.kronifyapis.dto.user.UserUpdateResponse
import co.com.kronifyapis.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @PatchMapping("/update")
    fun updateUser(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserUpdateResponse> {
        val updatedUser = userService.updateUser(user.userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    @PatchMapping("/updatePassword")
    fun updatePassword(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: UserChangePasswordRequest
    ): ResponseEntity<UserChangePasswordResponse> {
        val changedPassword = userService.changePassword(user.userId, request)
        return ResponseEntity.ok(changedPassword)
    }

}
