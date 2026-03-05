package com.example.viagourmet.Presentacion.screens.cuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.domain.model.DetallePedido
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

enum class OpcionHorario(val label: String, val minutos: Int) {
    AHORA("Ahora", 0),
    MINUTOS_25("25 minutos", 25),
    MINUTOS_30("30 minutos", 30),
    MINUTOS_45("45 minutos", 45),
    UNA_HORA("1 hora", 60)
}
data class CuentaUiState(
    val items: List<DetallePedido> = emptyList(),
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val iva: BigDecimal = BigDecimal.ZERO,
    val total: BigDecimal = BigDecimal.ZERO,
    val horaSeleccionada: OpcionHorario? = null,
    val isLoading: Boolean = false,
    val mensajeExito: String? = null,
    val errorMessage: String? = null
)

sealed class CuentaEvent {
    data class AgregarProducto(val producto: Producto, val cantidad: Int) : CuentaEvent()
    data class EliminarItem(val itemId: Int) : CuentaEvent()
    data class ActualizarCantidad(val itemId: Int, val nuevaCantidad: Int) : CuentaEvent()
    data class SeleccionarHorario(val opcion: OpcionHorario) : CuentaEvent()
    object SolicitarMesero : CuentaEvent()
    object PedirCuenta : CuentaEvent()
    object LimpiarCuenta : CuentaEvent()
}

@HiltViewModel
class CuentaViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CuentaUiState())
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    private val IVA_RATE = BigDecimal("0.16")

    fun onEvent(event: CuentaEvent) {
        when (event) {
            is CuentaEvent.AgregarProducto -> agregarProducto(event.producto, event.cantidad)
            is CuentaEvent.EliminarItem -> eliminarItem(event.itemId)
            is CuentaEvent.ActualizarCantidad -> actualizarCantidad(event.itemId, event.nuevaCantidad)
            is CuentaEvent.SeleccionarHorario -> _uiState.value = _uiState.value.copy(horaSeleccionada = event.opcion)
            is CuentaEvent.SolicitarMesero -> solicitarMesero()
            is CuentaEvent.PedirCuenta -> pedirCuenta()
            is CuentaEvent.LimpiarCuenta -> limpiarCuenta()
        }
    }

    private fun agregarProducto(producto: Producto, cantidad: Int) {
        viewModelScope.launch {
            val itemExistente = _uiState.value.items.find { it.productoId == producto.id }
            if (itemExistente != null) {
                actualizarCantidad(itemExistente.id, itemExistente.cantidad + cantidad)
            } else {
                val nuevoDetalle = DetallePedido(
                    id = UUID.randomUUID().hashCode(),
                    pedidoId = 0,
                    productoId = producto.id,
                    cantidad = cantidad,
                    precioUnitario = producto.precio,
                    notas = null,
                    producto = producto
                )
                _uiState.value = _uiState.value.copy(
                    items = _uiState.value.items + nuevoDetalle,
                    mensajeExito = "${producto.nombre} agregado al pedido"
                )
                calcularTotales()
            }
        }
    }

    private fun eliminarItem(itemId: Int) {
        viewModelScope.launch {
            val itemEliminado = _uiState.value.items.find { it.id == itemId }
            _uiState.value = _uiState.value.copy(
                items = _uiState.value.items.filter { it.id != itemId },
                mensajeExito = itemEliminado?.producto?.let { "${it.nombre} eliminado del pedido" }
            )
            calcularTotales()
        }
    }

    private fun actualizarCantidad(itemId: Int, nuevaCantidad: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                items = _uiState.value.items.map { detalle ->
                    if (detalle.id == itemId) detalle.copy(cantidad = nuevaCantidad) else detalle
                }
            )
            calcularTotales()
        }
    }

    private fun calcularTotales() {
        val subtotal = _uiState.value.items.sumOf { it.subtotal }
        val iva = subtotal.multiply(IVA_RATE)
        _uiState.value = _uiState.value.copy(
            subtotal = subtotal,
            iva = iva,
            total = subtotal + iva
        )
    }

    private fun solicitarMesero() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                mensajeExito = "🚀 Mesero solicitado, en breve te atenderán"
            )
        }
    }

    private fun pedirCuenta() {
        viewModelScope.launch {
            val hora = _uiState.value.horaSeleccionada
            _uiState.value = _uiState.value.copy(
                mensajeExito = "🧾 Pedido confirmado para ${hora?.label ?: "ahora"}"
            )
        }
    }

    private fun limpiarCuenta() {
        viewModelScope.launch {
            _uiState.value = CuentaUiState()
        }
    }
}