package mx.utng.carh.meserowatch.mobile

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Nuevo : Screen("nuevo", "Nuevo", Icons.Default.Add)
    object MisPedidos : Screen("mis_pedidos", "Mis pedidos", Icons.Default.Checklist)
    object Mesas : Screen("mesas", "Mesas", Icons.Default.TableBar)
    object Alertas : Screen("alertas", "Alertas", Icons.Default.Add) // Usar icono adecuado
    object Menu : Screen("menu", "Menú", Icons.Default.RestaurantMenu)
    object Personal : Screen("personal", "Personal", Icons.Default.Group)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Nuevo, Screen.Alertas, Screen.Mesas, Screen.Menu, Screen.Personal)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title, color = Color.Gray) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color(0xFF1E293B)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Nuevo.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Nuevo.route) { NuevoPedidoScreen() }
            composable(Screen.Alertas.route) { AlertasScreen() }
            composable(Screen.MisPedidos.route) { Text("Mis Pedidos Screen", color = Color.White) }
            composable(Screen.Mesas.route) { EstadoMesasScreen() }
            composable(Screen.Menu.route) { MenuAdminScreen() }
            composable(Screen.Personal.route) { PersonalAdminScreen() }
        }
    }
}
