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
    val pedido: Pedido? = null,
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
        observarPedidoActivo()
    }

    private fun observarPedidoActivo() {
        val clienteId = sessionManager.obtenerSesion()?.id ?: 0

        repository.getPedidosFlow()
            .onEach { pedidos ->
                val pedidoActivo = pedidos
                    .filter { it.clienteId == clienteId }
                    .filter {
                        it.estado != EstadoPedido.ENTREGADO &&
                                it.estado != EstadoPedido.CANCELADO
                    }
                    .maxByOrNull { it.id }

                _uiState.value = MiPedidoUiState(
                    pedido = pedidoActivo,
                    isLoading = false,
                    noPedidoActivo = pedidoActivo == null
                )
            }
            .launchIn(viewModelScope)
    }
}