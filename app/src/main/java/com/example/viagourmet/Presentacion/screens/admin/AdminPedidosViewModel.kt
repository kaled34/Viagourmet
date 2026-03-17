package com.example.viagourmet.Presentacion.screens.admin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.data.mock.AdminMockData
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.Pedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Filtro de pestañas para el admin
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
            FiltroAdmin.PENDIENTE -> pedidos.filter { it.estado == EstadoPedido.PENDIENTE }
            FiltroAdmin.EN_PREPARACION -> pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION }
            FiltroAdmin.LISTO -> pedidos.filter { it.estado == EstadoPedido.LISTO }
            FiltroAdmin.HISTORIAL -> pedidos.filter {
                it.estado == EstadoPedido.ENTREGADO || it.estado == EstadoPedido.CANCELADO
            }
        }

    val contadorPendientes: Int
        get() = pedidos.count { it.estado == EstadoPedido.PENDIENTE }

    val contadorEnPreparacion: Int
        get() = pedidos.count { it.estado == EstadoPedido.EN_PREPARACION }

    val contadorListos: Int
        get() = pedidos.count { it.estado == EstadoPedido.LISTO }
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
class AdminPedidosViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPedidosUiState(isLoading = true))
    val uiState: StateFlow<AdminPedidosUiState> = _uiState.asStateFlow()

    init {
        cargarPedidos()
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            is AdminEvent.Cargar -> cargarPedidos()
            is AdminEvent.SeleccionarFiltro -> _uiState.value =
                _uiState.value.copy(filtroSeleccionado = event.filtro)
            is AdminEvent.VerDetalle -> _uiState.value =
                _uiState.value.copy(pedidoSeleccionado = event.pedido, showDetalle = true)
            is AdminEvent.CerrarDetalle -> _uiState.value =
                _uiState.value.copy(showDetalle = false, pedidoSeleccionado = null)
            is AdminEvent.CambiarEstado -> cambiarEstado(event.pedidoId, event.nuevoEstado)
            is AdminEvent.LimpiarMensaje -> _uiState.value =
                _uiState.value.copy(mensajeExito = null, errorMessage = null)
        }
    }

    private fun cargarPedidos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(600) // Simula latencia de red
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                pedidos = AdminMockData.pedidos.toList()
            )
        }
    }

    private fun cambiarEstado(pedidoId: Int, nuevoEstado: EstadoPedido) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActualizando = true)
            delay(400) // Simula llamada al API

            val exito = AdminMockData.actualizarEstado(pedidoId, nuevoEstado)

            if (exito) {
                val pedidosActualizados = AdminMockData.pedidos.toList()
                val pedidoActualizado = pedidosActualizados.find { it.id == pedidoId }
                _uiState.value = _uiState.value.copy(
                    isActualizando = false,
                    pedidos = pedidosActualizados,
                    pedidoSeleccionado = pedidoActualizado,
                    mensajeExito = "Estado actualizado a: ${nuevoEstado.displayName()}"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isActualizando = false,
                    errorMessage = "No se pudo actualizar el estado"
                )
            }
        }
    }
}

fun EstadoPedido.displayName(): String = when (this) {
    EstadoPedido.PENDIENTE -> "Pendiente"
    EstadoPedido.EN_PREPARACION -> "En preparación"
    EstadoPedido.LISTO -> "Listo"
    EstadoPedido.ENTREGADO -> "Entregado"
    EstadoPedido.CANCELADO -> "Cancelado"
}

fun EstadoPedido.siguienteEstado(): EstadoPedido? = when (this) {
    EstadoPedido.PENDIENTE -> EstadoPedido.EN_PREPARACION
    EstadoPedido.EN_PREPARACION -> EstadoPedido.LISTO
    EstadoPedido.LISTO -> EstadoPedido.ENTREGADO
    EstadoPedido.ENTREGADO -> null
    EstadoPedido.CANCELADO -> null
}

fun ModuloPedido.displayName(): String = when (this) {
    ModuloPedido.DESAYUNOS -> "Desayunos"
    ModuloPedido.COMIDAS -> "Comidas"
    ModuloPedido.LIBRE -> "Libre"
}