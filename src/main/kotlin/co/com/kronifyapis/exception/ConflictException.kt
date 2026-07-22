package co.com.kronifyapis.exception

/**
 * Excepción lanzada cuando hay un conflicto con el estado actual del recurso,
 * por ejemplo, al intentar crear algo que ya existe. Devuelve 409.
 */
class ConflictException(message: String) : RuntimeException(message)
