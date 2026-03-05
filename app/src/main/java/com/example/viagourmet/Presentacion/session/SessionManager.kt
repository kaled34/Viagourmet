package com.example.viagourmet.Presentacion.session

import android.content.Context
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
    private var usuarioActual: UsuarioSesion? = null

    fun guardarSesion(usuario: UsuarioSesion) {
        usuarioActual = usuario
    }

    fun obtenerSesion(): UsuarioSesion? = usuarioActual

    fun cerrarSesion() {
        usuarioActual = null
    }

    fun estaLogueado(): Boolean = usuarioActual != null

    fun esCliente(): Boolean = usuarioActual?.rol == RolUsuario.CLIENTE

    fun esEmpleado(): Boolean = usuarioActual?.rol == RolUsuario.EMPLEADO
}