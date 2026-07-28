package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.service.FileStorageService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class UploadController(
    private val fileStorageService: FileStorageService
) {
    @PostMapping("/upload")
    fun uploadFile(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @RequestParam("file") file: MultipartFile,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        val filename = fileStorageService.store(file)
        val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}${request.contextPath}"
        val fileUrl = "$baseUrl/uploads/$filename"
        return ResponseEntity.ok(mapOf("url" to fileUrl))
    }
}
