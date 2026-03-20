package com.example.viagourmet.data.local.dao

import androidx.room.*
import com.example.viagourmet.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    /** Registrar un usuario nuevo. Lanza SQLiteConstraintException si el email ya existe. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    /** Buscar usuario por email y hash de contraseña (login). */
    @Query("SELECT * FROM usuarios WHERE email = :email AND passwordHash = :hash AND activo = 1 LIMIT 1")
    suspend fun login(email: String, hash: String): UsuarioEntity?

    /** Buscar usuario solo por email (para verificar si ya existe). */
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UsuarioEntity?

    /** Obtener usuario por id. */
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): UsuarioEntity?

    /** Actualizar datos del perfil (sin tocar password ni rol). */
    @Query("UPDATE usuarios SET nombre = :nombre, apellido = :apellido, telefono = :telefono WHERE id = :id")
    suspend fun updatePerfil(id: Int, nombre: String, apellido: String?, telefono: String?): Int

    /** Desactivar cuenta. */
    @Query("UPDATE usuarios SET activo = 0 WHERE id = :id")
    suspend fun desactivar(id: Int): Int
}