package com.example.viagourmet.Presentacion.navegacion

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.viagourmet.Presentacion.screens.admin.AdminPedidosScreen
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaEvent
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaScreen
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaViewModel
import com.example.viagourmet.Presentacion.screens.login.LoginScreen
import com.example.viagourmet.Presentacion.screens.menu.MenuScreen
import com.example.viagourmet.Presentacion.screens.menu.ProductoDetalleScreen
import com.example.viagourmet.Presentacion.screens.mipedido.MiPedidoScreen
import com.example.viagourmet.Presentacion.screens.registro.RegistroScreen
import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.SessionManager
import javax.inject.Inject

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registro : Screen("registro")
    object Menu : Screen("menu")
    object Cuenta : Screen("cuenta")
    object Admin : Screen("admin")
    object MiPedido : Screen("mi_pedido")
    object ProductoDetalle : Screen("producto/{productoId}") {
        fun createRoute(productoId: Int) = "producto/$productoId"
    }
}

@Composable
fun NavegacionGraph(
    sessionManager: SessionManager   // ← inyectado desde MainActivity
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { rol ->
                    when (rol) {
                        RolUsuario.CLIENTE -> navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        RolUsuario.EMPLEADO -> navController.navigate(Screen.Admin.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegistro = { navController.navigate(Screen.Registro.route) }
            )
        }

        composable(Screen.Registro.route) {
            RegistroScreen(
                onRegistroExitoso = { rol ->
                    when (rol) {
                        RolUsuario.CLIENTE -> navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Registro.route) { inclusive = true }
                        }
                        RolUsuario.EMPLEADO -> navController.navigate(Screen.Admin.route) {
                            popUpTo(Screen.Registro.route) { inclusive = true }
                        }
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
                },
                onCerrarSesion = {
                    sessionManager.cerrarSesion()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ProductoDetalle.route,
            arguments = listOf(navArgument("productoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val cuentaViewModel: CuentaViewModel = hiltViewModel()
            val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0

            ProductoDetalleScreen(
                productoId = productoId,
                onNavigateBack = { navController.popBackStack() },
                onAgregarAlPedido = { producto, cantidad ->
                    cuentaViewModel.onEvent(CuentaEvent.AgregarProducto(producto, cantidad))
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Cuenta.route) {
            CuentaScreen(
                onNavigateBack = { navController.popBackStack() },
                onSeguirComprando = { navController.popBackStack() },
                onVerEstadoPedido = { _ ->
                    navController.navigate(Screen.MiPedido.route) {
                        popUpTo(Screen.Menu.route)
                    }
                }
            )
        }

        composable(Screen.MiPedido.route) {
            MiPedidoScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Admin.route) {
            AdminPedidosScreen(
                onCerrarSesion = {
                    sessionManager.cerrarSesion()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Admin.route) { inclusive = true }
                    }
                }
            )
        }
    }
}