package com.example.viagourmet.data.repository

import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import com.example.viagourmet.data.local.dao.UsuarioDao
import com.example.viagourmet.data.local.mapper.crearUsuarioEntity
import com.example.viagourmet.data.local.mapper.toSesion
import com.example.viagourmet.data.local.util.hashPassword
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/** Resultado de una operación de autenticación. */
sealed class AuthResult {
    data class Success(val usuario: UsuarioSesion) : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val dao: UsuarioDao
) {

    // ── Login ─────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email y contraseña son obligatorios")
        }

        val emailNorm = email.trim().lowercase()
        val hash = hashPassword(password)

        // Primero verificar si el usuario existe (sin importar password)
        val existe = dao.findByEmail(emailNorm)
            ?: return AuthResult.Error("No existe una cuenta con ese email")

        // Verificar password
        val usuario = dao.login(emailNorm, hash)
            ?: return AuthResult.Error("Contraseña incorrecta")

        return AuthResult.Success(usuario.toSesion())
    }

    // ── Registro ──────────────────────────────────────────────────────────────

    suspend fun registrar(
        nombre: String,
        apellido: String?,
        telefono: String?,
        email: String,
        password: String,
        confirmarPassword: String,
        rol: RolUsuario
    ): AuthResult {
        // Validaciones
        if (nombre.isBlank()) return AuthResult.Error("El nombre es obligatorio")
        if (email.isBlank() || !email.contains("@")) return AuthResult.Error("Email inválido")
        if (password.length < 6) return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        if (password != confirmarPassword) return AuthResult.Error("Las contraseñas no coinciden")

        val emailNorm = email.trim().lowercase()

        if (dao.findByEmail(emailNorm) != null) {
            return AuthResult.Error("Ya existe una cuenta con ese email")
        }

        return try {
            val entity = crearUsuarioEntity(
                nombre = nombre.trim(),
                apellido = apellido?.trim()?.ifBlank { null },
                telefono = telefono?.trim()?.ifBlank { null },
                email = emailNorm,
                password = password,
                rol = rol,
                ahora = nowString()
            )
            val id = dao.insertUsuario(entity).toInt()

            AuthResult.Success(
                UsuarioSesion(
                    id = id,
                    nombre = nombre.trim(),
                    email = emailNorm,
                    rol = rol
                )
            )
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            AuthResult.Error("Ya existe una cuenta con ese email")
        } catch (e: Exception) {
            AuthResult.Error("Error al crear la cuenta: ${e.message}")
        }
    }

    //

    private fun nowString(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d-%02dT%02d:%02d:%02d".format(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            c.get(Calendar.SECOND)
        )
    }
}