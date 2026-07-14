package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.user.UserChangePasswordRequest
import co.com.kronifyapis.dto.user.UserChangePasswordResponse
import co.com.kronifyapis.dto.user.UserUpdateRequest
import co.com.kronifyapis.dto.user.UserUpdateResponse
import co.com.kronifyapis.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @PatchMapping("/update/{userId}")
    fun updateUser(
        @PathVariable userId: UUID,
        @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserUpdateResponse> {
        val updatedUser = userService.updateUser(userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    @PatchMapping("/updatePassword/{userId}")
    fun updatePassword(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UserChangePasswordRequest
    ): ResponseEntity<UserChangePasswordResponse> {
        val changedPassword = userService.changePassword(userId, request)
        return ResponseEntity.ok(changedPassword)
    }

}