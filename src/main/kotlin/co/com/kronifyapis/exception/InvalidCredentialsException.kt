package co.com.kronifyapis.exception

/**
 * Excepción lanzada cuando el correo o la contraseña son incorrectos
 * durante el inicio de sesión. Devuelve 401.
 */
class InvalidCredentialsException (message: String) : RuntimeException(message)