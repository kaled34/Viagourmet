package com.example.viagourmet.Presentacion.screens.mipedido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class MiPedidoUiState(
    val pedidoActivo: Pedido? = null,       // pedido en curso (Pendiente / En prep / Listo)
    val ultimoEntregado: Pedido? = null,    // último pedido entregado (para mostrar historial)
    val isLoading: Boolean = false,
    val noPedidoActivo: Boolean = false
)

@HiltViewModel
class MiPedidoViewModel @Inject constructor(
    private val repository: PedidoRepositoryLocal,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiPedidoUiState(isLoading = true))
    val uiState: StateFlow<MiPedidoUiState> = _uiState.asStateFlow()

    init {
        observarPedidos()
    }

    private fun observarPedidos() {
        val clienteId = sessionManager.obtenerSesion()?.id ?: 0

        repository.getPedidosFlow()
            .onEach { pedidos ->
                // Todos los pedidos de este cliente
                val misPedidos = pedidos.filter { it.clienteId == clienteId }

                // Pedido activo: el más reciente que no esté entregado ni cancelado
                val activo = misPedidos
                    .filter {
                        it.estado != EstadoPedido.ENTREGADO &&
                                it.estado != EstadoPedido.CANCELADO
                    }
                    .maxByOrNull { it.id }

                // Último entregado: para mostrar cuando no hay activo
                val ultimoEntregado = misPedidos
                    .filter { it.estado == EstadoPedido.ENTREGADO }
                    .maxByOrNull { it.id }

                _uiState.value = MiPedidoUiState(
                    pedidoActivo = activo,
                    ultimoEntregado = ultimoEntregado,
                    isLoading = false,
                    noPedidoActivo = activo == null
                )
            }
            .launchIn(viewModelScope)
    }
}