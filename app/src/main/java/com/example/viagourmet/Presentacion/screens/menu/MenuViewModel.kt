package com.example.viagourmet.Presentacion.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.data.mock.MockData
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val isLoading: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val productos: List<Producto> = emptyList(),
    val categoriaSeleccionada: Categoria? = null,
    val errorMessage: String? = null
)

sealed class MenuEvent {
    data class SeleccionarCategoria(val categoria: Categoria) : MenuEvent()
    object CargarMenu : MenuEvent()
}

@HiltViewModel
class MenuViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState(isLoading = true))
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        cargarMenu()
    }

    fun onEvent(event: MenuEvent) {
        when (event) {
            is MenuEvent.SeleccionarCategoria -> seleccionarCategoria(event.categoria)
            is MenuEvent.CargarMenu -> cargarMenu()
        }
    }

    private fun cargarMenu() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val categorias = MockData.getCategoriasActivas()
                val primeraCategoria = categorias.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categorias = categorias,
                    categoriaSeleccionada = primeraCategoria,
                    productos = if (primeraCategoria != null)
                        MockData.getProductosByCategoria(primeraCategoria.id)
                    else
                        emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el menú: ${e.message}"
                )
            }
        }
    }

    private fun seleccionarCategoria(categoria: Categoria) {
        _uiState.value = _uiState.value.copy(
            categoriaSeleccionada = categoria,
            productos = MockData.getProductosByCategoria(categoria.id)
        )
    }
}