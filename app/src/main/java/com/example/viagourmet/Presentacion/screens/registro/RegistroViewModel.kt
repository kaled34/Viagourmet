package com.example.viagourmet.Presentacion.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistroUiState(
    val isLoading: Boolean = false,
    val registroExitoso: Boolean = false,
    val rolRegistrado: RolUsuario? = null,
    val errorMessage: String? = null
)

sealed class RegistroEvent {
    data class Registrar(
        val nombre: String,
        val apellido: String?,
        val telefono: String?,
        val email: String,
        val password: String,
        val rol: RolUsuario
    ) : RegistroEvent()
    object LimpiarError : RegistroEvent()
}

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegistroEvent) {
        when (event) {
            is RegistroEvent.Registrar -> registrar(event)
            is RegistroEvent.LimpiarError -> _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    private fun registrar(event: RegistroEvent.Registrar) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Simulamos llamada al API
                delay(1000)

                // Guardamos sesión con el rol seleccionado
                sessionManager.guardarSesion(
                    UsuarioSesion(
                        id = 1, // temporal hasta tener API
                        nombre = event.nombre,
                        email = event.email,
                        rol = event.rol
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    registroExitoso = true,
                    rolRegistrado = event.rol
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al registrarse: ${e.message}"
                )
            }
        }
    }
}