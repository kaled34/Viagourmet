package com.example.viagourmet.Presentacion.screens.mipedido

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.Presentacion.theme.GreenSuccess
import com.example.viagourmet.domain.model.EstadoPedido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPedidoScreen(
    viewModel: MiPedidoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de mi pedido") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Brown80,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hora de recogida
            uiState.horaRecogida?.let { hora ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Brown80.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🕐 Hora de recogida",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = hora,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Brown80
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timeline de estados
            Text(
                text = "Estado del pedido",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val estados = listOf(
                EstadoPedido.PENDIENTE to "Pedido recibido",
                EstadoPedido.EN_PREPARACION to "En preparación",
                EstadoPedido.LISTO to "¡Listo para recoger!"
            )

            estados.forEach { (estado, label) ->
                val isActual = uiState.estadoActual == estado
                val isPasado = estados.indexOfFirst { it.first == uiState.estadoActual } >=
                        estados.indexOfFirst { it.first == estado }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Círculo indicador
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = when {
                            isActual -> GreenSuccess
                            isPasado -> Brown80
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(20.dp)
                    ) {}

                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isPasado)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.outline
                    )

                    if (isActual) {
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = GreenSuccess.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Actual",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (estado != EstadoPedido.LISTO) {
                    Box(
                        modifier = Modifier
                            .padding(start = 9.dp)
                            .width(2.dp)
                            .height(24.dp)
                    ) {
                        Surface(
                            color = if (isPasado) Brown80 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }
            }

            // Si el pedido está listo
            if (uiState.estadoActual == EstadoPedido.LISTO) {
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = GreenSuccess.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "✅ ¡Tu pedido está listo! Pasa a recogerlo.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GreenSuccess,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}