package com.example.viagourmet.Presentacion.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Extensiones de pedido en PedidoExtensions.kt

enum class FiltroAdmin(val label: String) {
    TODOS("Todos"),
    PENDIENTE("Pendientes"),
    EN_PREPARACION("En prep."),
    LISTO("Listos"),
    HISTORIAL("Historial")
}

data class AdminPedidosUiState(
    val isLoading: Boolean = false,
    val pedidos: List<Pedido> = emptyList(),
    val filtroSeleccionado: FiltroAdmin = FiltroAdmin.TODOS,
    val pedidoSeleccionado: Pedido? = null,
    val showDetalle: Boolean = false,
    val isActualizando: Boolean = false,
    val mensajeExito: String? = null,
    val errorMessage: String? = null
) {
    val pedidosFiltrados: List<Pedido>
        get() = when (filtroSeleccionado) {
            FiltroAdmin.TODOS -> pedidos.filter {
                it.estado != EstadoPedido.ENTREGADO && it.estado != EstadoPedido.CANCELADO
            }
            FiltroAdmin.PENDIENTE      -> pedidos.filter { it.estado == EstadoPedido.PENDIENTE }
            FiltroAdmin.EN_PREPARACION -> pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION }
            FiltroAdmin.LISTO          -> pedidos.filter { it.estado == EstadoPedido.LISTO }
            FiltroAdmin.HISTORIAL      -> pedidos.filter {
                it.estado == EstadoPedido.ENTREGADO || it.estado == EstadoPedido.CANCELADO
            }
        }

    val contadorPendientes: Int      get() = pedidos.count { it.estado == EstadoPedido.PENDIENTE }
    val contadorEnPreparacion: Int   get() = pedidos.count { it.estado == EstadoPedido.EN_PREPARACION }
    val contadorListos: Int          get() = pedidos.count { it.estado == EstadoPedido.LISTO }
}

sealed class AdminEvent {
    object Cargar : AdminEvent()
    data class SeleccionarFiltro(val filtro: FiltroAdmin) : AdminEvent()
    data class VerDetalle(val pedido: Pedido) : AdminEvent()
    object CerrarDetalle : AdminEvent()
    data class CambiarEstado(val pedidoId: Int, val nuevoEstado: EstadoPedido) : AdminEvent()
    object LimpiarMensaje : AdminEvent()
}

@HiltViewModel
class AdminPedidosViewModel @Inject constructor(
    private val repository: PedidoRepositoryLocal
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPedidosUiState(isLoading = true))
    val uiState: StateFlow<AdminPedidosUiState> = _uiState.asStateFlow()

    init {
        observarPedidos()
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            is AdminEvent.Cargar          -> Unit
            is AdminEvent.SeleccionarFiltro -> _uiState.value =
                _uiState.value.copy(filtroSeleccionado = event.filtro)
            is AdminEvent.VerDetalle      -> _uiState.value =
                _uiState.value.copy(pedidoSeleccionado = event.pedido, showDetalle = true)
            is AdminEvent.CerrarDetalle   -> _uiState.value =
                _uiState.value.copy(showDetalle = false, pedidoSeleccionado = null)
            is AdminEvent.CambiarEstado   -> cambiarEstado(event.pedidoId, event.nuevoEstado)
            is AdminEvent.LimpiarMensaje  -> _uiState.value =
                _uiState.value.copy(mensajeExito = null, errorMessage = null)
        }
    }

    private fun observarPedidos() {
        repository.getPedidosFlow()
            .onEach { pedidos ->
                val idSeleccionado = _uiState.value.pedidoSeleccionado?.id
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pedidos = pedidos.sortedByDescending { it.id },
                    pedidoSeleccionado = if (idSeleccionado != null)
                        pedidos.find { it.id == idSeleccionado }
                    else
                        _uiState.value.pedidoSeleccionado
                )
            }
            .launchIn(viewModelScope)
    }

    private fun cambiarEstado(pedidoId: Int, nuevoEstado: EstadoPedido) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActualizando = true)
            val exito = repository.actualizarEstado(pedidoId, nuevoEstado)
            _uiState.value = _uiState.value.copy(
                isActualizando = false,
                mensajeExito = if (exito) "Actualizado a: ${nuevoEstado.displayName()}" else null,
                errorMessage  = if (!exito) "No se pudo actualizar" else null
            )
        }
    }
}