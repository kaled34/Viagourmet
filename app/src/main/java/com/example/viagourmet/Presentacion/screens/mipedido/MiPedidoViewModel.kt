package com.example.viagourmet.Presentacion.screens.mipedido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.domain.model.EstadoPedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MiPedidoUiState(
    val estadoActual: EstadoPedido = EstadoPedido.PENDIENTE,
    val horaRecogida: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class MiPedidoViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MiPedidoUiState())
    val uiState: StateFlow<MiPedidoUiState> = _uiState.asStateFlow()

    init {
        simularActualizacionEstado()
    }

    private fun simularActualizacionEstado() {
        viewModelScope.launch {
            // Simulamos polling al API cada 10 segundos
            // Cuando tengas API real, reemplaza con Flow o WebSocket
            _uiState.value = _uiState.value.copy(
                horaRecogida = "13:30",
                estadoActual = EstadoPedido.PENDIENTE
            )
            delay(10_000)
            _uiState.value = _uiState.value.copy(estadoActual = EstadoPedido.EN_PREPARACION)
            delay(10_000)
            _uiState.value = _uiState.value.copy(estadoActual = EstadoPedido.LISTO)
        }
    }
    
}