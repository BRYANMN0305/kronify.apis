package co.com.kronifyapis.exception

/**
 * Excepción lanzada cuando la solicitud del cliente tiene datos inválidos
 * o falta algún campo obligatorio. Devuelve 400.
 */
class BadRequestException(message: String) : RuntimeException(message)
