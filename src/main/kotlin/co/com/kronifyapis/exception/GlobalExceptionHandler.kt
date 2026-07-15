package co.com.kronifyapis.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(
        ex: InvalidCredentialsException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.message, request)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.NOT_FOUND, ex.message, request)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.NOT_FOUND, ex.message, request)
    }

    @ExceptionHandler(BadRequestException::class, TypeErrorException::class)
    fun handleBadRequest(
        ex: RuntimeException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.message, request)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(
        ex: ConflictException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.CONFLICT, ex.message, request)
    }

    @ExceptionHandler(ForbiddenOperationException::class)
    fun handleForbiddenOperationException(
        ex: ForbiddenOperationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.FORBIDDEN, ex.message, request)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .firstOrNull()
            ?.defaultMessage
            ?: "Validation error"
        return buildResponse(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado, error del servidor", request)
    }

    private fun buildResponse(
        status: HttpStatus,
        message: String?,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.status(status).body(body)
    }
}
