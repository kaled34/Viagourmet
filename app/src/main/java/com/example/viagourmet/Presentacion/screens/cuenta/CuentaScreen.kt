package com.example.viagourmet.Presentacion.screens.cuenta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.Presentacion.theme.GreenSuccess
import com.example.viagourmet.domain.model.DetallePedido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaScreen(
    viewModel: CuentaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSeguirComprando: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Brown80,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    ResumenCuenta(
                        subtotal = uiState.subtotal,
                        iva = uiState.iva,
                        total = uiState.total
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.onEvent(CuentaEvent.SolicitarMesero) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mesero")
                        }

                        Button(
                            onClick = { viewModel.onEvent(CuentaEvent.PedirCuenta) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenSuccess
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cuenta")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onSeguirComprando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seguir agregando productos")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tu cuenta está vacía",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Agrega productos del menú",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onSeguirComprando) {
                        Text("Ver menú")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.items,
                    key = { detalle: DetallePedido -> detalle.id }
                ) { detalle: DetallePedido ->
                    ItemCuentaCard(
                        detalle = detalle,
                        onEliminar = {
                            viewModel.onEvent(CuentaEvent.EliminarItem(detalle.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCuentaCard(
    detalle: DetallePedido,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detalle.producto?.nombre ?: "Producto",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${detalle.cantidad} x $${"%.2f".format(detalle.precioUnitario)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "$${"%.2f".format(detalle.subtotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Brown80
                )
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ResumenCuenta(
    subtotal: java.math.BigDecimal,
    iva: java.math.BigDecimal,
    total: java.math.BigDecimal,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
                Text("$${"%.2f".format(subtotal)}", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("IVA (16%)", style = MaterialTheme.typography.bodyLarge)
                Text("$${"%.2f".format(iva)}", style = MaterialTheme.typography.bodyLarge)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOTAL", style = MaterialTheme.typography.titleLarge)
                Text(
                    "$${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Brown80
                )
            }
        }
    }
}