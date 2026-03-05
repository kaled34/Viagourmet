package com.example.viagourmet.Presentacion.navegacion

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaEvent
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaScreen
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaViewModel
import com.example.viagourmet.Presentacion.screens.login.LoginScreen
import com.example.viagourmet.Presentacion.screens.menu.MenuScreen
import com.example.viagourmet.Presentacion.screens.menu.ProductoDetalleScreen
import com.example.viagourmet.Presentacion.screens.mipedido.MiPedidoScreen
import com.example.viagourmet.Presentacion.screens.registro.RegistroScreen
import com.example.viagourmet.Presentacion.session.RolUsuario


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registro : Screen("registro")
    object Menu : Screen("menu")
    object Cuenta : Screen("cuenta")
    object MiPedido : Screen("mi_pedido")
    object ProductoDetalle : Screen("producto/{productoId}") {
        fun createRoute(productoId: Int) = "producto/$productoId"
    }
}

@Composable
fun NavegacionGraph() {
    val navController = rememberNavController()
    val cuentaViewModel: CuentaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { rol ->
                    if (rol == RolUsuario.CLIENTE) {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        // TODO: navegar a pantalla de empleado
                    }
                },
                onNavigateToRegistro = {
                    navController.navigate(Screen.Registro.route)
                }
            )
        }

        composable(Screen.Registro.route) {
            RegistroScreen(
                onRegistroExitoso = { rol ->
                    if (rol == RolUsuario.CLIENTE) {
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Registro.route) { inclusive = true }
                        }
                    } else {

                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

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

        composable(
            route = Screen.ProductoDetalle.route,
            arguments = listOf(navArgument("productoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
            ProductoDetalleScreen(
                productoId = productoId,
                onNavigateBack = { navController.popBackStack() },
                onAgregarAlPedido = { producto, cantidad ->
                    cuentaViewModel.onEvent(CuentaEvent.AgregarProducto(producto, cantidad))
                }
            )
        }

        composable(Screen.Cuenta.route) {
            CuentaScreen(
                viewModel = cuentaViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSeguirComprando = { navController.popBackStack() },
                onVerEstadoPedido = {
                    navController.navigate(Screen.MiPedido.route)
                }
            )
        }

        composable(Screen.MiPedido.route) {
            MiPedidoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}