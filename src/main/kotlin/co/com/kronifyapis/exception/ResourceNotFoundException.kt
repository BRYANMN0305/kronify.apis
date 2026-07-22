package co.com.kronifyapis.exception

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado. Devuelve 404.
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)