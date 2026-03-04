package com.example.viagourmet.presentation.screens.cuenta

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

// Estado de la cuenta actual
data class CuentaUiState(
    val items: List<DetallePedido> = emptyList(),
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val iva: BigDecimal = BigDecimal.ZERO,
    val total: BigDecimal = BigDecimal.ZERO,
    val isLoading: Boolean = false,
    val mensajeExito: String? = null,
    val errorMessage: String? = null
)

// Eventos de la cuenta
sealed class CuentaEvent {
    data class AgregarProducto(val producto: Producto, val cantidad: Int) : CuentaEvent()
    data class EliminarItem(val itemId: Int) : CuentaEvent()
    data class ActualizarCantidad(val itemId: Int, val nuevaCantidad: Int) : CuentaEvent()
    object SolicitarMesero : CuentaEvent()
    object PedirCuenta : CuentaEvent()
    object LimpiarCuenta : CuentaEvent()
}

@HiltViewModel
class CuentaViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CuentaUiState())
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    // Tasa de IVA (16% como en tu BD)
    private val IVA_RATE = BigDecimal("0.16")

    fun onEvent(event: CuentaEvent) {
        when (event) {
            is CuentaEvent.AgregarProducto -> agregarProducto(event.producto, event.cantidad)
            is CuentaEvent.EliminarItem -> eliminarItem(event.itemId)
            is CuentaEvent.ActualizarCantidad -> actualizarCantidad(event.itemId, event.nuevaCantidad)
            is CuentaEvent.SolicitarMesero -> solicitarMesero()
            is CuentaEvent.PedirCuenta -> pedirCuenta()
            is CuentaEvent.LimpiarCuenta -> limpiarCuenta()
        }
    }

    private fun agregarProducto(producto: Producto, cantidad: Int) {
        viewModelScope.launch {
            // Verificar si el producto ya está en el carrito
            val itemExistente = _uiState.value.items.find { it.productoId == producto.id }

            if (itemExistente != null) {
                // Si ya existe, actualizar cantidad
                val nuevaCantidad = itemExistente.cantidad + cantidad
                actualizarCantidad(itemExistente.id, nuevaCantidad)
            } else {
                // Si no existe, crear nuevo detalle
                val nuevoDetalle = DetallePedido(
                    id = UUID.randomUUID().hashCode(), // ID temporal
                    pedidoId = 0, // Temporal
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
                mensajeExito = itemEliminado?.producto?.let {
                    "${it.nombre} eliminado del pedido"
                }
            )
            calcularTotales()
        }
    }

    private fun actualizarCantidad(itemId: Int, nuevaCantidad: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                items = _uiState.value.items.map { detalle ->
                    if (detalle.id == itemId) {
                        detalle.copy(cantidad = nuevaCantidad)
                    } else {
                        detalle
                    }
                }
            )
            calcularTotales()
        }
    }

    private fun calcularTotales() {
        val subtotal = _uiState.value.items.sumOf { it.subtotal }
        val iva = subtotal.multiply(IVA_RATE)
        val total = subtotal + iva

        _uiState.value = _uiState.value.copy(
            subtotal = subtotal,
            iva = iva,
            total = total
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
            _uiState.value = _uiState.value.copy(
                mensajeExito = "🧾 Cuenta solicitada, por favor espera"
            )
        }
    }

    private fun limpiarCuenta() {
        viewModelScope.launch {
            _uiState.value = CuentaUiState()
        }
    }
}