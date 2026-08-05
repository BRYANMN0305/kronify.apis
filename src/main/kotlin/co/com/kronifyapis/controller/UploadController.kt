package co.com.kronifyapis.controller

import co.com.kronifyapis.dto.auth.AuthenticatedUser
import co.com.kronifyapis.service.FileStorageService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    /**
     * Sirve archivos subidos de forma segura.
     * El Content-Type se resuelve SOLO por extensión dentro de una whitelist
     * (nunca del Content-Type enviado por el cliente), y se envían cabeceras
     * que impiden el sniffing de MIME y la ejecución de scripts (nosniff + CSP sandbox).
     * SVG, HTML y cualquier extensión desconocida NO se sirven.
     */
    @GetMapping("/uploads/{filename:.+}")
    fun serveFile(@PathVariable("filename") filename: String): ResponseEntity<Resource> {
        val resolved = fileStorageService.resolvePublicFile(filename)
            ?: return ResponseEntity.notFound().build()

        val (path, contentType) = resolved
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, contentType)
            .header("X-Content-Type-Options", "nosniff")
            .header("Content-Security-Policy", "sandbox; default-src 'none'; img-src 'self' data:; style-src 'self' 'unsafe-inline'")
            .header("Cache-Control", "public, max-age=3600")
            .contentType(MediaType.parseMediaType(contentType))
            .body(FileSystemResource(path))
    }
}