package com.example.viagourmet.data.repository

import com.example.viagourmet.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidoRepositoryLocal @Inject constructor() {

    // ── Pedidos confirmados ──────────────────────────────────────────────────
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    // ── Carrito compartido (persiste entre pantallas) ────────────────────────
    private val _carrito = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val carrito: StateFlow<List<ItemCarrito>> = _carrito.asStateFlow()

    private var nextId = 1

    // ── Carrito: agregar producto ────────────────────────────────────────────

    fun agregarAlCarrito(producto: Producto, cantidad: Int) {
        _carrito.update { lista ->
            val existente = lista.find { it.producto.id == producto.id }
            if (existente != null) {
                lista.map {
                    if (it.producto.id == producto.id)
                        it.copy(cantidad = it.cantidad + cantidad)
                    else it
                }
            } else {
                lista + ItemCarrito(
                    id = System.currentTimeMillis().toInt(),
                    producto = producto,
                    cantidad = cantidad
                )
            }
        }
    }

    fun eliminarDelCarrito(itemId: Int) {
        _carrito.update { lista -> lista.filter { it.id != itemId } }
    }

    fun actualizarCantidadCarrito(itemId: Int, nuevaCantidad: Int) {
        _carrito.update { lista ->
            lista.map { if (it.id == itemId) it.copy(cantidad = nuevaCantidad) else it }
        }
    }

    fun limpiarCarrito() {
        _carrito.value = emptyList()
    }

    // ── Pedidos: crear ───────────────────────────────────────────────────────

    fun crearPedido(
        clienteId: Int,
        clienteNombre: String,
        horario: String?,
        notas: String?
    ): Pedido {
        val itemsActuales = _carrito.value
        val detalles = itemsActuales.mapIndexed { index, item ->
            DetallePedido(
                id = index + 1,
                pedidoId = nextId,
                productoId = item.producto.id,
                cantidad = item.cantidad,
                precioUnitario = item.producto.precio,
                notas = null,
                producto = item.producto
            )
        }

        val nuevoPedido = Pedido(
            id = nextId++,
            empleadoId = 0,
            clienteId = clienteId,
            modulo = detectarModulo(itemsActuales),
            estado = EstadoPedido.PENDIENTE,
            tipo = TipoPedido.PARA_LLEVAR,
            horarioRecogidaId = null,
            notas = buildString {
                if (!horario.isNullOrBlank()) append("🕐 Recogida: $horario")
                if (!notas.isNullOrBlank()) {
                    if (isNotBlank()) append(" | ")
                    append(notas)
                }
            }.ifBlank { null },
            creadoEn = nowAsLocalDateTime(),
            actualizadoEn = nowAsLocalDateTime(),
            detalles = detalles,
            itemsLibres = emptyList(),
            cliente = Cliente(
                id = clienteId,
                nombre = clienteNombre,
                apellido = null,
                telefono = null,
                email = null
            )
        )

        _pedidos.update { lista -> lista + nuevoPedido }
        limpiarCarrito()
        return nuevoPedido
    }

    // ── Pedidos: cambiar estado ──────────────────────────────────────────────

    fun actualizarEstado(pedidoId: Int, nuevoEstado: EstadoPedido): Boolean {
        var exito = false
        _pedidos.update { lista ->
            lista.map { pedido ->
                if (pedido.id == pedidoId) {
                    exito = true
                    pedido.copy(
                        estado = nuevoEstado,
                        actualizadoEn = nowAsLocalDateTime()
                    )
                } else pedido
            }
        }
        return exito
    }

    fun getPedidosFlow(): StateFlow<List<Pedido>> = pedidos

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun detectarModulo(items: List<ItemCarrito>): ModuloPedido {
        val categorias = items.mapNotNull { it.producto.categoria?.modulo }
        return when {
            categorias.all { it == ModuloCategoria.DESAYUNOS } -> ModuloPedido.DESAYUNOS
            categorias.all { it == ModuloCategoria.COMIDAS } -> ModuloPedido.COMIDAS
            else -> ModuloPedido.LIBRE
        }
    }

    private fun nowAsLocalDateTime(): java.time.LocalDateTime {
        val c = java.util.Calendar.getInstance()
        val iso = "%04d-%02d-%02dT%02d:%02d:%02d".format(
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH),
            c.get(java.util.Calendar.HOUR_OF_DAY),
            c.get(java.util.Calendar.MINUTE),
            c.get(java.util.Calendar.SECOND)
        )
        return java.time.LocalDateTime.parse(iso)
    }
}

// Modelos auxiliares
data class ItemCarrito(
    val id: Int,
    val producto: Producto,
    val cantidad: Int
)

// Mantenemos ItemPedido por compatibilidad
data class ItemPedido(
    val producto: Producto,
    val cantidad: Int
)