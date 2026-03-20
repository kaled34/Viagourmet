package com.example.viagourmet.data.local.dao

import androidx.room.*
import com.example.viagourmet.data.entity.PedidoEntity
import com.example.viagourmet.data.local.entity.DetallePedidoEntity
import com.example.viagourmet.data.local.entity.PedidoConDetalles
import com.example.viagourmet.data.local.entity.PedidoLibreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {


    /** Todos los pedidos con sus detalles, ordenados del más reciente al más antiguo. */
    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY id DESC")
    fun getAllPedidosFlow(): Flow<List<PedidoConDetalles>>

    /** Pedidos activos (no entregados ni cancelados). */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE estado NOT IN ('ENTREGADO','CANCELADO') ORDER BY id DESC")
    fun getPedidosActivosFlow(): Flow<List<PedidoConDetalles>>

    /** Pedidos de un cliente específico. */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId ORDER BY id DESC")
    fun getPedidosByClienteFlow(clienteId: Int): Flow<List<PedidoConDetalles>>

    /** Un pedido por id. */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    suspend fun getPedidoById(pedidoId: Int): PedidoConDetalles?

    // ── Inserciones ──────────────────────────────────────────────────────────

    /** Inserta el pedido y devuelve el id generado por Room. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPedido(pedido: PedidoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetalles(detalles: List<DetallePedidoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemsLibres(items: List<PedidoLibreEntity>)

    // ── Actualizaciones ──────────────────────────────────────────────────────

    @Query("UPDATE pedidos SET estado = :estado, actualizadoEn = :ahora WHERE id = :pedidoId")
    suspend fun actualizarEstado(pedidoId: Int, estado: String, ahora: String): Int

    // ── Eliminaciones ────────────────────────────────────────────────────────

    @Query("DELETE FROM pedidos")
    suspend fun deleteAll()
}