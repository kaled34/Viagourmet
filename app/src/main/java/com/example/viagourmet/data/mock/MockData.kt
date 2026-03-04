package com.example.viagourmet.data.mock

import com.example.viagourmet.domain.model.*
import java.math.BigDecimal

object MockData {

    val categorias = listOf(
        Categoria(
            id = 1,
            nombre = "Desayunos",
            descripcion = "Comienza tu día con energía",
            modulo = ModuloCategoria.DESAYUNOS,
            activo = true
        ),
        Categoria(
            id = 2,
            nombre = "Comidas",
            descripcion = "Platillos fuertes para el hambre",
            modulo = ModuloCategoria.COMIDAS,
            activo = true
        ),
        Categoria(
            id = 3,
            nombre = "Bebidas",
            descripcion = "Refresca tu paladar",
            modulo = ModuloCategoria.LIBRE,
            activo = true
        )
    )

    val productos = listOf(
        // Desayunos (categoría 1)
        Producto(
            id = 1,
            categoriaId = 1,
            nombre = "Café Americano",
            descripcion = "Café recién hecho, aroma intenso",
            precio = BigDecimal("35.00"),
            disponible = true,
            imagenUrl = null,
            categoria = categorias[0]
        ),
        Producto(
            id = 2,
            categoriaId = 1,
            nombre = "Hot Cakes",
            descripcion = "Tres hot cakes con miel y mantequilla",
            precio = BigDecimal("85.00"),
            disponible = true,
            imagenUrl = null,
            categoria = categorias[0]
        ),
        Producto(
            id = 3,
            categoriaId = 1,
            nombre = "Chilaquiles",
            descripcion = "Con pollo, crema y queso fresco",
            precio = BigDecimal("95.00"),
            disponible = true,
            imagenUrl = null,
            categoria = categorias[0]
        ),
        // Comidas (categoría 2)
        Producto(
            id = 4,
            categoriaId = 2,
            nombre = "Hamburguesa Clásica",
            descripcion = "120g de carne, queso, lechuga y tomate",
            precio = BigDecimal("120.00"),
            disponible = true,
            imagenUrl = null,
            categoria = categorias[1]
        ),
        Producto(
            id = 5,
            categoriaId = 2,
            nombre = "Pechuga a la plancha",
            descripcion = "Con verduras salteadas y arroz",
            precio = BigDecimal("135.00"),
            disponible = true,
            imagenUrl = null,
            categoria = categorias[1]
        ),
        // Bebidas (categoría 3)
        Producto(
            id = 6,
            categoriaId = 3,
            nombre = "Limonada",
            descripcion = "Natural o mineral",
            precio = BigDecimal("40.00"),
            disponible = true,
            imagenUrl = null,
            categoria = categorias[2]
        )
    )

    fun getCategoriasActivas(): List<Categoria> {
        return categorias.filter { it.activo }
    }

    fun getProductosByCategoria(categoriaId: Int): List<Producto> {
        return productos.filter { it.categoriaId == categoriaId && it.disponible }
    }
}