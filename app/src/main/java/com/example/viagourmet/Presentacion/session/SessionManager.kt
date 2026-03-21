package com.example.viagourmet.Presentacion.session

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class RolUsuario { CLIENTE, EMPLEADO }

data class UsuarioSesion(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: RolUsuario
)

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vg_session", Context.MODE_PRIVATE)


    private var usuarioActual: UsuarioSesion? = null

    fun guardarSesion(usuario: UsuarioSesion) {
        usuarioActual = usuario
        prefs.edit()
            .putInt("user_id", usuario.id)
            .putString("user_nombre", usuario.nombre)
            .putString("user_email", usuario.email)
            .putString("user_rol", usuario.rol.name)
            .apply()
    }

    fun obtenerSesion(): UsuarioSesion? = usuarioActual

    fun cerrarSesion() {
        usuarioActual = null
        prefs.edit().clear().apply()
    }

    fun estaLogueado(): Boolean = usuarioActual != null
    fun esCliente(): Boolean = usuarioActual?.rol == RolUsuario.CLIENTE
    fun esEmpleado(): Boolean = usuarioActual?.rol == RolUsuario.EMPLEADO
}