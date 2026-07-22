package co.com.kronifyapis.exception

/**
 * Excepción lanzada cuando el usuario no tiene permisos para realizar
 * una operación. Devuelve 403.
 */
class ForbiddenOperationException(message: String) : RuntimeException(message)
