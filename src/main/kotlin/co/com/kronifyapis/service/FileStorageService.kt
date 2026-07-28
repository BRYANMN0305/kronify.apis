package co.com.kronifyapis.service

import co.com.kronifyapis.exception.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class FileStorageService(
    @Value("\${app.upload.path}") private val uploadPath: String
) {
    private val allowedContentTypes = setOf("image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml")
    private val uploadDir: Path = Path.of(uploadPath).toAbsolutePath().normalize()

    init {
        Files.createDirectories(uploadDir)
    }

    fun store(file: MultipartFile): String {
        if (file.isEmpty) throw BadRequestException("El archivo está vacío")

        val contentType = file.contentType
            ?: throw BadRequestException("No se pudo determinar el tipo del archivo")

        if (contentType !in allowedContentTypes) {
            throw BadRequestException("Tipo de archivo no permitido: $contentType. Solo se aceptan imágenes (JPEG, PNG, WebP, GIF, SVG)")
        }

        val extension = when (contentType) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            "image/svg+xml" -> ".svg"
            else -> ".bin"
        }

        val filename = "${UUID.randomUUID()}$extension"
        val targetPath = uploadDir.resolve(filename)

        try {
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: IOException) {
            throw RuntimeException("Error al guardar el archivo", e)
        }

        return filename
    }
}
