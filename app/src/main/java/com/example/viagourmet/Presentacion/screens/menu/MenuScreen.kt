package com.example.viagourmet.Presentacion.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.components.CategoriaChip
import com.example.viagourmet.Presentacion.components.ProductoCard
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.Producto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel = hiltViewModel(),
    onNavigateToDetalle: (Int) -> Unit,
    onNavigateToCuenta: () -> Unit
) {
    // SIN collectAsStateWithLifecycle — usamos collectAsState que no necesita dependencia extra
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Via Gourmet",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Brown80
                ),
                actions = {
                    IconButton(onClick = onNavigateToCuenta) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ver cuenta",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.onEvent(MenuEvent.CargarMenu) }) {
                            Text("Reintentar")
                        }
                    }
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.categorias,
                                key = { categoria: Categoria -> categoria.id }
                            ) { categoria: Categoria ->
                                CategoriaChip(
                                    categoria = categoria,
                                    isSelected = uiState.categoriaSeleccionada?.id == categoria.id,
                                    onClick = {
                                        viewModel.onEvent(MenuEvent.SeleccionarCategoria(categoria))
                                    }
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.productos,
                                key = { producto: Producto -> producto.id }
                            ) { producto: Producto ->
                                ProductoCard(
                                    producto = producto,
                                    onClick = { onNavigateToDetalle(producto.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}