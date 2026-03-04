package com.example.viagourmet.Presentation.navigacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.viagourmet.Presentacion.screens.login.LoginScreen
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaScreen
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaViewModel
import com.example.viagourmet.Presentacion.screens.menu.MenuScreen
import com.example.viagourmet.Presentation.screens.menu.ProductoDetalleScreen


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Menu : Screen("menu")
    object Cuenta : Screen("cuenta")
    object ProductoDetalle : Screen("producto/{productoId}") {
        fun createRoute(productoId: Int) = "producto/$productoId"
    }
}

@Composable
fun NavegacionGraph() {
    val navController = rememberNavController()

    // Compartir el mismo ViewModel de cuenta entre pantallas
    val cuentaViewModel: CuentaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Pantalla de Login
        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Screen.Menu.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
                onNavigateToRegistro = {
                    // TODO: Implementar registro
                }
            )

        }

        // Pantalla de Menú
        composable(Screen.Menu.route) {
            MenuScreen(
                onNavigateToDetalle = { productoId ->
                    navController.navigate(Screen.ProductoDetalle.createRoute(productoId))
                },
                onNavigateToCuenta = {
                    navController.navigate(Screen.Cuenta.route)
                }
            )
        }

        // Pantalla de Detalle de Producto
        composable(
            route = Screen.ProductoDetalle.route,
            arguments = listOf(navArgument("productoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0

            ProductoDetalleScreen(
                productoId = productoId,
                onNavigateBack = { navController.popBackStack() },
                onAgregarAlPedido = { producto, cantidad ->
                    cuentaViewModel.onEvent(
                        com.example.viagourmet.Presentacion.screens.cuenta.CuentaEvent.AgregarProducto(
                            producto, cantidad
                        )
                    )
                }
            )
        }

        // Pantalla de Cuenta
        composable(Screen.Cuenta.route) {
            CuentaScreen(
                viewModel = cuentaViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSeguirComprando = { navController.popBackStack() }
            )
        }
    }
}