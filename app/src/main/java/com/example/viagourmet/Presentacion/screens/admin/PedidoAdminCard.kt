package com.example.viagourmet.Presentacion.screens.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import java.time.LocalDateTime
import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido


@Composable
fun PedidoAdminCard(
    pedido: Pedido,
    onClick: () -> Unit,
    onAvanzarEstado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estadoColor by animateColorAsState(
        targetValue = pedido.estado.colorFondo(),
        animationSpec = tween(300),
        label = "estadoColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, pedido.estado.colorBorde()),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Header: estado + módulo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(estadoColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pedido.estado.icono(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = pedido.estado.displayName(),
                        style = MaterialTheme.typography.labelLarge,
                        color = pedido.estado.colorTexto(),
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = pedido.modulo.colorFondo(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = pedido.modulo.displayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = pedido.modulo.colorTexto(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Body: info del pedido
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pedido #${pedido.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pedido.creadoEn.toHoraString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cliente
                pedido.cliente?.let { cliente ->
                    Text(
                        text = "👤 ${cliente.nombre} ${cliente.apellido ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Resumen de productos
                val resumen = buildString {
                    val items = pedido.detalles.mapNotNull { it.producto?.nombre }
                        .plus(pedido.itemsLibres.map { it.descripcion })
                    append(items.take(2).joinToString(", "))
                    if (items.size > 2) append(" +${items.size - 2} más")
                }
                if (resumen.isNotBlank()) {
                    Text(
                        text = resumen,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Notas importantes
                if (!pedido.notas.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFE65100)
                        )
                        Text(
                            text = pedido.notas,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE65100),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Tipo de pedido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (pedido.tipo.name == "PARA_LLEVAR") "🛍 Para llevar" else "🏢 Oficina",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Botón avanzar estado
                    val siguiente = pedido.estado.siguienteEstado()
                    if (siguiente != null) {
                        FilledTonalButton(
                            onClick = onAvanzarEstado,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = siguiente.colorFondo().copy(alpha = 0.9f),
                                contentColor = siguiente.colorTexto()
                            )
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = siguiente.displayName(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

// === Extensiones de color por estado ===

fun EstadoPedido.colorFondo(): Color = when (this) {
    EstadoPedido.PENDIENTE -> Color(0xFFFFF3E0)
    EstadoPedido.EN_PREPARACION -> Color(0xFFE3F2FD)
    EstadoPedido.LISTO -> Color(0xFFE8F5E9)
    EstadoPedido.ENTREGADO -> Color(0xFFF5F5F5)
    EstadoPedido.CANCELADO -> Color(0xFFFFEBEE)
}

fun EstadoPedido.colorBorde(): Color = when (this) {
    EstadoPedido.PENDIENTE -> Color(0xFFFF6F00)
    EstadoPedido.EN_PREPARACION -> Color(0xFF1565C0)
    EstadoPedido.LISTO -> Color(0xFF2E7D32)
    EstadoPedido.ENTREGADO -> Color(0xFF9E9E9E)
    EstadoPedido.CANCELADO -> Color(0xFFC62828)
}

fun EstadoPedido.colorTexto(): Color = when (this) {
    EstadoPedido.PENDIENTE -> Color(0xFFE65100)
    EstadoPedido.EN_PREPARACION -> Color(0xFF0D47A1)
    EstadoPedido.LISTO -> Color(0xFF1B5E20)
    EstadoPedido.ENTREGADO -> Color(0xFF616161)
    EstadoPedido.CANCELADO -> Color(0xFFB71C1C)
}

fun EstadoPedido.icono(): String = when (this) {
    EstadoPedido.PENDIENTE -> "🟡"
    EstadoPedido.EN_PREPARACION -> "🔵"
    EstadoPedido.LISTO -> "🟢"
    EstadoPedido.ENTREGADO -> "✅"
    EstadoPedido.CANCELADO -> "❌"
}

fun com.example.viagourmet.domain.model.ModuloPedido.colorFondo(): Color = when (this) {
    com.example.viagourmet.domain.model.ModuloPedido.DESAYUNOS -> Color(0xFFFFF8E1)
    com.example.viagourmet.domain.model.ModuloPedido.COMIDAS -> Color(0xFFF3E5F5)
    com.example.viagourmet.domain.model.ModuloPedido.LIBRE -> Color(0xFFE0F2F1)
}

fun com.example.viagourmet.domain.model.ModuloPedido.colorTexto(): Color = when (this) {
    com.example.viagourmet.domain.model.ModuloPedido.DESAYUNOS -> Color(0xFFF57F17)
    com.example.viagourmet.domain.model.ModuloPedido.COMIDAS -> Color(0xFF6A1B9A)
    com.example.viagourmet.domain.model.ModuloPedido.LIBRE -> Color(0xFF00695C)
}

// === Helpers compatibles con minSdk 24 ===


fun LocalDateTime.toHoraString(): String {
    val cal = toCalendar()
    return "%02d:%02d".format(
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE)
    )
}

fun LocalDateTime.toFechaHoraString(): String {
    val cal = toCalendar()
    return "%02d/%02d/%04d %02d:%02d".format(
        cal.get(Calendar.DAY_OF_MONTH),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.YEAR),
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE)
    )
}

private fun LocalDateTime.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    // Usamos toString() para parsear manualmente y evitar campos API 26+
    // LocalDateTime.toString() devuelve "yyyy-MM-ddTHH:mm:ss[.nanos]"
    val parts = toString().split("T")
    val dateParts = parts[0].split("-")
    val timeParts = parts[1].split(":")
    cal.set(
        dateParts[0].toInt(),           // year
        dateParts[1].toInt() - 1,       // month (0-based)
        dateParts[2].toInt(),           // day
        timeParts[0].toInt(),           // hour
        timeParts[1].toInt(),           // minute
        timeParts[2].substringBefore(".").toIntOrNull() ?: 0  // second
    )
    return cal
}