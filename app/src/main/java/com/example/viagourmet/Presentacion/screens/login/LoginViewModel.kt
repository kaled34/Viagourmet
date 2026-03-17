package com.example.viagourmet.Presentacion.screens.login

import androidx.lifecycle.ViewModel
import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginExitoso: Boolean = false,
    val rolLogueado: RolUsuario? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, rol: RolUsuario) {
        // Simulamos autenticación — aquí irá la llamada al API real
        // Generamos un id único basado en el email para distinguir clientes
        val clienteId = email.hashCode().let { if (it < 0) -it else it } % 10000 + 1

        sessionManager.guardarSesion(
            UsuarioSesion(
                id = clienteId,
                nombre = email.substringBefore("@"),
                email = email,
                rol = rol
            )
        )

        _uiState.value = LoginUiState(
            loginExitoso = true,
            rolLogueado = rol
        )
    }
}