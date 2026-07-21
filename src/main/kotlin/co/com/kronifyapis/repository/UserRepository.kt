package co.com.kronifyapis.repository

import co.com.kronifyapis.model.User
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio que gestiona los usuarios del sistema.
 * Permite buscar por ID, por email y verificar si un email ya está registrado.
 */

interface UserRepository : JpaRepository<User, Long> {

    //Busca un usuario por su ID
    fun findByUserId(userId: Long): User?

    //Busca un usuario por su email
    fun findByEmail(email: String): User?

    //Verifica si un email ya está registrado
    fun existsByEmail(email: String): Boolean





}
